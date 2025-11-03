package com.example.appmusic_basico;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Repeat;
import com.spotify.protocol.types.Track;


public class ThirdActivity extends AppCompatActivity {

    private static final String TAG = "ThirdActivity";

    private ImageButton playPauseButton, skipNextButton, skipPrevButton;
    private ImageButton minimizeButton, repeatButton, stopButton, moreVertButton;
    private SeekBar seekBar;
    private TextView tvSongTitle, tvArtistName, tvCurrentTime, tvTotalTime;
    private ImageView ivAlbumArt;

    private SpotifyAppRemote spotifyAppRemote;
    private Subscription<PlayerState> mPlayerStateSubscription;
    private boolean isSeeking = false;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private Runnable seekBarUpdateRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.music_play);

        // --- Inicializar vistas ---
        playPauseButton = findViewById(R.id.ib_play_pause_icon);
        skipPrevButton = findViewById(R.id.ib_skip_anterior);
        skipNextButton = findViewById(R.id.ib_skip_next);
        minimizeButton = findViewById(R.id.iv_chevron_down);
        repeatButton = findViewById(R.id.ib_repeat);
        stopButton = findViewById(R.id.ib_stop_playback);
        moreVertButton = findViewById(R.id.iv_more_vertical);

        seekBar = findViewById(R.id.sk_seek_bar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Solo actualiza la etiqueta de tiempo mientras el usuario arrastra
                if (fromUser) {
                    tvCurrentTime.setText(formatTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // 💡 Detiene la actualización automática cuando el usuario comienza a arrastrar
                isSeeking = true;
                stopSeekBarUpdate();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 💡 Envía la nueva posición de reproducción a Spotify
                if (spotifyAppRemote != null) {
                    spotifyAppRemote.getPlayerApi().seekTo(seekBar.getProgress());
                }

                // 💡 Reinicia la actualización automática después de buscar
                isSeeking = false;
                // Reiniciamos el PlayerState para obtener la posición exacta y relanzar el handler
                spotifyAppRemote.getPlayerApi().getPlayerState().setResultCallback(playerState -> {
                    updateUI(playerState);
                });
            }
        });

        tvSongTitle = findViewById(R.id.tv_title_song);
        tvArtistName = findViewById(R.id.tv_name_artist);
        tvCurrentTime = findViewById(R.id.tv_current_time);
        tvTotalTime = findViewById(R.id.tv_total_time);
        ivAlbumArt = findViewById(R.id.iv_album_art);

        spotifyAppRemote = MainActivity.getSpotifyAppRemote();

        if (spotifyAppRemote == null) {
            Log.e(TAG, "❌ FATAL: SpotifyAppRemote es NULL en ThirdActivity.");
            Toast.makeText(this, "Error: Conéctate a Spotify primero.", Toast.LENGTH_LONG).show();
            // Podrías considerar finalizar la actividad aquí si no hay conexión
            // finish();
        } else {
            // Suscribirse solo si está conectado
            subscribeToPlayerState();
        }

        // Obtener los datos del Intent
        Intent intent = getIntent();
        String trackName = intent.getStringExtra("TRACK_NAME");
        String artistName = intent.getStringExtra("ARTIST_NAME");
        String trackUri = intent.getStringExtra("TRACK_URI");
        //boolean isPlaying = intent.getBooleanExtra("IS_PLAYING", false);

        // --- Actualizar la UI con los datos de la canción ---
        tvSongTitle.setText(trackName);
        tvArtistName.setText(artistName);

        playPauseButton.setOnClickListener(v -> {
            if (MainActivity.playlistManager != null) {
                MainActivity.playlistManager.togglePlayPause();
            } else {
                Log.e(TAG, "❌ PlaylistManager es null. Activando reconexión.");
                Toast.makeText(this, "Conectando Spotify...", Toast.LENGTH_SHORT).show();

                // 🚀 SOLUCIÓN: Si falla, activa la bandera y termina ThirdActivity
                MainActivity.shouldReconnectSpotify = true;
                finish(); // Cierra ThirdActivity para que MainActivity recupere el foco y ejecute onStart
            }
        });

        skipNextButton.setOnClickListener(v -> {
            if (MainActivity.playlistManager != null) {
                MainActivity.playlistManager.playNext();
            } else {
                Log.e(TAG, "❌ PlaylistManager es null. Activando reconexión.");
                Toast.makeText(this, "Conectando Spotify...", Toast.LENGTH_SHORT).show();

                // 🚀 SOLUCIÓN: Si falla, activa la bandera y termina ThirdActivity
                MainActivity.shouldReconnectSpotify = true;
                finish(); // Cierra ThirdActivity para que MainActivity recupere el foco y ejecute onStart
            }
        });

        skipPrevButton.setOnClickListener(v -> {
            if (MainActivity.playlistManager != null) {
                MainActivity.playlistManager.playPrevious();
            } else {
                Log.e(TAG, "❌ PlaylistManager es null. Activando reconexión.");
                Toast.makeText(this, "Conectando Spotify...", Toast.LENGTH_SHORT).show();

                // 🚀 SOLUCIÓN: Si falla, activa la bandera y termina ThirdActivity
                MainActivity.shouldReconnectSpotify = true;
                finish(); // Cierra ThirdActivity para que MainActivity recupere el foco y ejecute onStart
            }
        });

        minimizeButton.setOnClickListener(v -> finish());
        repeatButton.setOnClickListener(v -> toggleRepeat());
        stopButton.setOnClickListener(v -> stopPlayback());
        moreVertButton.setOnClickListener(this::showPopupMenu);

    }

    @Override
    protected void onStop() {
        super.onStop();
        stopSeekBarUpdate();
        // ✅ Esto cancela la suscripción para que no interfiera.
        unsubscribePlayerState();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 💡 DEBE VOLVER A SUSCRIBIRSE si la actividad regresa al frente
        spotifyAppRemote = MainActivity.getSpotifyAppRemote();
        subscribeToPlayerState();
    }

    // ========================================
    // PLAYERSTATE & UI
    // ========================================
    // ThirdActivity.java

    private void subscribeToPlayerState() {
        unsubscribePlayerState();

        // 💡 Paso 1: Asegurar que spotifyAppRemote tenga la referencia.
        // Aunque lo obtienes en onCreate, una comprobación aquí no está de más.
        if (spotifyAppRemote == null) {
            spotifyAppRemote = MainActivity.getSpotifyAppRemote();
        }

        if (spotifyAppRemote != null && spotifyAppRemote.isConnected()) { // 💡 CLAVE: Comprobar isConnected()
            mPlayerStateSubscription = (Subscription<PlayerState>) spotifyAppRemote.getPlayerApi()
                    .subscribeToPlayerState()
                    .setEventCallback(this::updateUI) // Llama a updateUI cada vez que hay un cambio
                    .setErrorCallback(error -> {
                        Log.e(TAG, "❌ Error PlayerState: La suscripción falló. " + error.getMessage());
                        // 💡 Añadir un Toast para alertar al usuario inmediatamente
                        Toast.makeText(this, "Error de sincronización con Spotify.", Toast.LENGTH_LONG).show();
                    });

            // 💡 CLAVE: Obtener el estado inicial inmediatamente.
            // Esto fuerza la primera actualización de la UI.
            spotifyAppRemote.getPlayerApi().getPlayerState().setResultCallback(this::updateUI);

        } else {
            Log.e(TAG, "❌ SpotifyAppRemote no conectado o null en ThirdActivity.");
            Toast.makeText(this, "Error: Spotify no está conectado. Intente reconectar en la pantalla principal.", Toast.LENGTH_LONG).show();
        }
    }

    // ThirdActivity.java

    private void unsubscribePlayerState() {
        // 1. Verificar si la suscripción existe y NO está cancelada.
        if (mPlayerStateSubscription != null && !mPlayerStateSubscription.isCanceled()) {

            // 2. Intentar cancelar SOLO si el remote NO es NULL.
            // NO podemos comprobar isConnected() directamente aquí porque el remote puede ser
            // nulo o desconectado, por lo que usamos un bloque try-catch como protección final.
            try {
                mPlayerStateSubscription.cancel();
                Log.d(TAG, "✅ PlayerState suscripción cancelada.");
            } catch (Exception e) {
                // Capturamos específicamente la excepción de terminación para evitar el crash.
                if (e instanceof com.spotify.android.appremote.api.error.SpotifyConnectionTerminatedException) {
                    Log.w(TAG, "⚠️ Error esperado: Conexión Spotify ya terminada al cancelar suscripción.");
                } else {
                    Log.e(TAG, "❌ Error inesperado al cancelar suscripción: " + e.getMessage());
                }
            }

            mPlayerStateSubscription = null;
        }
    }

    private void updateUI(PlayerState playerState) {
        if (playerState.track == null) {
            // Detener actualizaciones si no hay pista.
            stopSeekBarUpdate();
            return;
        }

        Track track = playerState.track;
        tvSongTitle.setText(track.name);
        tvArtistName.setText(track.artist.name);

        // ✅ Actualización del botón play/pause
        int playPauseRes = playerState.isPaused ? R.drawable.play_arrow_24dp : R.drawable.pause_24dp;
        playPauseButton.setImageResource(playPauseRes);

        // 3. 🚀 LÓGICA CLAVE: Actualizar el Ícono de Repetir
        int repeatMode = playerState.playbackOptions.repeatMode;
        int repeatIconResId;

        if (repeatMode == Repeat.ALL) {
            repeatIconResId = R.drawable.repeat_24dp; // Ícono para repetir toda la playlist
        } else if (repeatMode == Repeat.ONE) {
            repeatIconResId = R.drawable.repeat_one_24dp; // Ícono para repetir una sola canción
        } else { // Repeat.OFF
            repeatIconResId = R.drawable.repeat_off; // Ícono para repetición desactivada
        }

        // Asignar el recurso al botón de repetición
        repeatButton.setImageResource(repeatIconResId);

        Glide.with(this)
                .load(track.imageUri.toString())
                .placeholder(R.drawable.ic_launcher_background)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(ivAlbumArt);

        long duration = track.duration;
        seekBar.setMax((int) duration);
        tvTotalTime.setText(formatTime(duration));

        // 💡 GESTIÓN DE LA SEEKBAR:
        if (!isSeeking) {
            // 1. Actualiza la posición de la barra usando el estado del reproductor.
            seekBar.setProgress((int) playerState.playbackPosition);
            tvCurrentTime.setText(formatTime(playerState.playbackPosition));

            // 2. Si se está reproduciendo, inicia el Runnable para que la barra se mueva más suavemente
            //    (ya que PlayerState no se actualiza cada milisegundo).
            if (!playerState.isPaused) {
                startSeekBarUpdate(playerState.playbackPosition);
            } else {
                stopSeekBarUpdate();
            }
        } else {
            // Si el usuario está arrastrando, detenemos el Runnable para que no interfiera.
            stopSeekBarUpdate();
        }
    }


    private String formatTime(long ms) {
        long seconds = (ms / 1000) % 60;
        long minutes = (ms / (1000 * 60)) % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    // ========================================
    // PLAYBACK CONTROLS
    // ========================================
    private void togglePlayPause() {
        if (MainActivity.playlistManager != null) {
            MainActivity.playlistManager.togglePlayPause();
        }
    }

    private void playNext() {
        if (MainActivity.playlistManager != null) {
            MainActivity.playlistManager.playNext();
        }
    }

    private void playPrevious() {
        if (MainActivity.playlistManager != null) {
            MainActivity.playlistManager.playPrevious();
        }
    }

    private void toggleRepeat() {
        if (MainActivity.playlistManager == null) {
            Toast.makeText(this, "Error: Reproductor no inicializado.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Delegar la acción al Manager
        MainActivity.playlistManager.toggleRepeat();

        // 2. Opcional: Forzar una actualización de la UI después de un breve retraso
        //    para asegurar que el PlayerState refleje el cambio
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            SpotifyAppRemote remote = MainActivity.getSpotifyAppRemote();
            if (remote != null) {
                // Esto dispara updateUI() y genera el Toast
                remote.getPlayerApi().getPlayerState().setResultCallback(playerState -> {
                    // Aquí podrías agregar el Toast basándote en playerState.playbackOptions.repeatMode
                    String toastMessage = getRepeatToastMessage(playerState.playbackOptions.repeatMode);
                    Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show();

                    // La función updateUI ya manejará el cambio de ícono
                    updateUI(playerState);
                });
            }
        }, 100); // Pequeño retraso para dar tiempo a Spotify a procesar el comando
    }

    // 💡 Nuevo método de utilidad para mostrar el mensaje
    private String getRepeatToastMessage(int repeatMode) {
        if (repeatMode == Repeat.ALL) {
            return "Repetir playlist";
        } else if (repeatMode == Repeat.ONE) {
            return "Repetir canción";
        } else {
            return "Repetir desactivado";
        }
    }

    private void stopPlayback() {
        // Usar la referencia estática para pausar
        SpotifyAppRemote remote = MainActivity.getSpotifyAppRemote();

        if (remote != null) {
            // Pausar la reproducción, no forzar el stop de la conexión remota.
            remote.getPlayerApi().pause();
        }
        // Finalizar la actividad de reproducción
        finish();
    }

    // ========================================
    // SEEK BAR
    // ========================================

    private void startSeekBarUpdate(long currentPosition) {
        // Si ya está corriendo, lo detenemos para reiniciarlo con la nueva posición.
        handler.removeCallbacks(seekBarUpdateRunnable);

        seekBarUpdateRunnable = new Runnable() {
            private long lastPosition = currentPosition;
            private long startTime = System.currentTimeMillis();

            @Override
            public void run() {
                if (!isSeeking && !spotifyAppRemote.isConnected()) return;

                // Calcula el tiempo transcurrido desde la última posición conocida
                long elapsedTime = System.currentTimeMillis() - startTime;
                long newPosition = lastPosition + elapsedTime;

                // Si supera la duración, detenemos la actualización
                if (newPosition >= seekBar.getMax()) {
                    stopSeekBarUpdate();
                    return;
                }

                // Actualizamos la UI
                seekBar.setProgress((int) newPosition);
                tvCurrentTime.setText(formatTime(newPosition));

                // Repetimos en 50 milisegundos para fluidez
                handler.postDelayed(this, 50);
            }
        };
        handler.post(seekBarUpdateRunnable);
    }

    private void stopSeekBarUpdate() {
        handler.removeCallbacks(seekBarUpdateRunnable);
    }

    // ========================================
    // MENÚ POPUP
    // ========================================
    private void showPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenuInflater().inflate(R.menu.menu_opciones_more_vert, popup.getMenu());
        popup.setOnMenuItemClickListener(this::handleMenuItemSelection);
        popup.show();
    }

    private boolean handleMenuItemSelection(MenuItem item) {
        String msg;
        int id = item.getItemId();
        if (id == R.id.opcion_favoritos) msg = "Favorito";
        else if (id == R.id.opcion_play_list) msg = "Agregar a Playlist";
        else if (id == R.id.opcion_fila) msg = "Agregar a la fila";
        else if (id == R.id.opcion_album) msg = "Ver álbum";
        else if (id == R.id.opcion_artista) msg = "Ver artista";
        else if (id == R.id.opcion_compartir) msg = "Compartir";
        else if (id == R.id.opcion_ocultar) msg = "Ocultar";
        else return false;

        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        return true;
    }

}
