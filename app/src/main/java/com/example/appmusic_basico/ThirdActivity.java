package com.example.appmusic_basico;

import android.content.Intent;
import android.content.SharedPreferences;
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
import com.example.appmusic_basico.api.RetrofitClient;
import com.example.appmusic_basico.api.SpotifyArtistSearchResponse;
import com.example.appmusic_basico.api.SpotifyService;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import models.AlbumFavorito;
import models.Artistas;
import models.SpotifyAlbumDetailsResponse;
import models.SpotifyArtistDetailsResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ThirdActivity extends AppCompatActivity {

    private static final String TAG = "ThirdActivity";
    private final Gson gson = new Gson();
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
    private String currentAlbumImageUrl = "";
    private String currentArtistSpotifyId = "";
    private String currentAlbumName = "";
    private String currentAlbumUri = "";

    private String spotifyId = "";


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
        //String trackUri = intent.getStringExtra("TRACK_URI");
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
    // =======================================

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

    //Metodo para cancelar la suscripcion

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

        // 💡 Actualización de variables de álbum
        if (track.album != null) {
            this.currentAlbumName = track.album.name;
            this.currentAlbumUri = track.album.uri;
            // ✅ Asegúrate de que track.album.uri NO es nulo antes de llamar a replace.
            if (track.album.uri != null && track.album.uri.startsWith("spotify:album:")) {
                // EXTRACCIÓN CORRECTA:
                this.spotifyId = track.album.uri.replace("spotify:album:", "");
            } else {
                // Fallback si el URI no es de álbum (ej. es un single con URI de pista)
                this.spotifyId = "";
            }
        } else {
            this.currentAlbumName = track.name; // Fallback
            this.currentAlbumUri = "";
            this.spotifyId = "";
        }

        if (track.imageUri != null) {

            this.currentAlbumImageUrl = track.imageUri.toString();

            currentArtistSpotifyId = track.artist.uri != null
                    ? track.artist.uri.replace("spotify:artist:", "")
                    : "";

            // 1. OBTENER LA URL: Usar el servicio de imágenes de Spotify para obtener una URL válida
            spotifyAppRemote.getImagesApi()
                    .getImage(track.imageUri) // Usamos el objeto Uri, no el String
                    .setResultCallback(bitmap -> {
                        // 2. CARGAR LA IMAGEN: Glide carga el Bitmap directamente.
                        Glide.with(this)
                                .asBitmap()
                                .load(bitmap)
                                .placeholder(R.drawable.ic_launcher_background)
                                .into(ivAlbumArt);

                    })
                    .setErrorCallback(throwable -> {
                        Log.e(TAG, "❌ Error al cargar imagen del álbum: " + throwable.getMessage());
                        // Si falla, usamos el placeholder y un URI vacío para favoritos
                        this.currentAlbumImageUrl = "";
                        ivAlbumArt.setImageResource(R.drawable.ic_launcher_background);
                    });
        } else {
            this.currentAlbumImageUrl = "";
            ivAlbumArt.setImageResource(R.drawable.ic_launcher_background);
        }

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

    private void toggleRepeat() {
        if (MainActivity.playlistManager == null) {
            Toast.makeText(this, "Error: Reproductor no inicializado.", Toast.LENGTH_SHORT).show();
            return;
        }

        MainActivity.playlistManager.toggleRepeat();
        Toast.makeText(this, "Canción reiniciada", Toast.LENGTH_SHORT).show();

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
        if (id == R.id.opcion_favoritos) {
            String currentArtistName = tvArtistName.getText().toString();
            String currentArtistImageUrl = currentAlbumImageUrl;
            String currentArtistId = currentArtistSpotifyId;


            Artistas newFavorite = new Artistas(currentArtistName, currentArtistImageUrl, currentArtistId);
            toggleFavoriteArtist(newFavorite);
            return true;
        }

        else if (id == R.id.opcion_play_list) msg = "Agregar a Playlist";
        else if (id == R.id.opcion_fila) msg = "Agregar a la fila";
        else if (id == R.id.opcion_album) {
            // NUEVA LÓGICA: Agregar álbum a favoritos
            String name = this.currentAlbumName;
            String artist = tvArtistName.getText().toString();
            String uri = this.currentAlbumUri;
            String imageUrl = this.currentAlbumImageUrl;
            String albumId = this.spotifyId;

            if (name.isEmpty() || artist.isEmpty() || uri.isEmpty() || albumId.isEmpty()) {
                Toast.makeText(this, "No hay información de álbum disponible.", Toast.LENGTH_SHORT).show();
                return true;
            }

            // Crear y guardar el álbum
            AlbumFavorito newFavoriteAlbum = new AlbumFavorito(name, artist, imageUrl, uri, albumId);
            toggleFavoriteAlbum(newFavoriteAlbum);
            return true;
        }
        else if (id == R.id.opcion_artista) msg = "Ver artista";
        else if (id == R.id.opcion_compartir) msg = "Compartir";
        else if (id == R.id.opcion_ocultar) msg = "Ocultar";
        else return false;

        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        return true;
    }

    private String getArtistImageUrlFromSubscription() {
        return this.currentAlbumImageUrl != null ? this.currentAlbumImageUrl : "";
    }

    // Método de Guardado para artistas favoritos
    private void toggleFavoriteArtist(Artistas artista) {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        // Gson ya está inicializado como campo de la clase: private final Gson gson = new Gson();

        // Leer lista actual
        String json = prefs.getString("favorite_artists_json", "[]");
        Type type = new TypeToken<List<Artistas>>() {}.getType();
        List<Artistas> favoritos = gson.fromJson(json, type);
        if (favoritos == null) favoritos = new ArrayList<>();

        // Buscar si ya está por el nombre (CRÍTICO)
        boolean exists = false;
        Artistas existingArtistInList = null; // Para guardar la referencia si existe
        for (Artistas a : favoritos) {
            if (a.getNombre() != null && a.getNombre().equalsIgnoreCase(artista.getNombre())) {
                exists = true;
                existingArtistInList = a; // Guardar la referencia existente
                break;
            }
        }

        if (exists) {
            // Eliminar si ya existe
            favoritos.remove(existingArtistInList);
            Toast.makeText(this, existingArtistInList.getNombre() + " eliminado de favoritos", Toast.LENGTH_SHORT).show();
            // Guardar cambios inmediatamente (commit) si se eliminó
            prefs.edit().putString("favorite_artists_json", gson.toJson(favoritos)).commit();
            Log.d(TAG, "Artist " + artista.getNombre() + " removed. JSON committed.");
            sendBroadcast(new Intent("ARTIST_FAVORITE_UPDATE")); // Notificar
        } else {
            // Si no existe, agregar y buscar imagen
            favoritos.add(artista); // Agregar el nuevo artista con info básica
            Toast.makeText(this, artista.getNombre() + " agregado a favoritos", Toast.LENGTH_SHORT).show();

            // 🚀 Obtener imagen real de Spotify usando searchArtists
            if ((artista.getImagenUrl() == null || artista.getImagenUrl().isEmpty()) // Si la URL está vacía al inicio
                    && MainActivity.spotifyAccessToken != null) {

                SpotifyService api = RetrofitClient.getClient().create(SpotifyService.class);
                final List<Artistas> finalFavoritos = favoritos; // Referencia final para el callback

                api.searchArtists("Bearer " + MainActivity.spotifyAccessToken, artista.getNombre().trim(), "artist")
                        .enqueue(new retrofit2.Callback<SpotifyArtistSearchResponse>() {
                            @Override
                            public void onResponse(retrofit2.Call<SpotifyArtistSearchResponse> call,
                                                   retrofit2.Response<SpotifyArtistSearchResponse> response) {
                                Log.d(TAG, "Spotify API code: " + response.code());
                                if (response.isSuccessful() && response.body() != null
                                        && response.body().getArtists() != null
                                        && !response.body().getArtists().getItems().isEmpty()) {
                                    Log.d(TAG, "Spotify raw body: " + new Gson().toJson(response.body()));

                                    SpotifyArtistSearchResponse.Item firstArtist =
                                            response.body().getArtists().getItems().get(0);

                                    if (firstArtist.getImages() != null && !firstArtist.getImages().isEmpty()) {
                                        String imageUrl = firstArtist.getImages().get(0).getUrl();

                                        // 💡 CRÍTICO: BUSCAR EL ARTISTA RECIÉN AGREGADO EN LA LISTA Y ACTUALIZARLO
                                        Artistas artistToUpdate = null;
                                        for (Artistas a : finalFavoritos) {
                                            // Usamos el nombre para encontrarlo, como en equals()
                                            if (a.getNombre() != null && a.getNombre().equalsIgnoreCase(artista.getNombre())) {
                                                artistToUpdate = a;
                                                break;
                                            }
                                        }

                                        if (artistToUpdate != null) {
                                            artistToUpdate.setImagenUrl(imageUrl); // <--- Actualiza la URL
                                            artistToUpdate.setIdSpotify(firstArtist.getId()); // También el ID
                                            Log.d(TAG, "✅ URL de artista '" + artistToUpdate.getNombre() + "' actualizada: " + imageUrl);

                                            // 🚨 GUARDAR LA LISTA COMPLETA ACTUALIZADA CON COMMIT
                                            String jsonUpdated = gson.toJson(finalFavoritos);
                                            Log.e("PERSISTENCE_CHECK", "JSON con URL actualizada a guardar: " + jsonUpdated);
                                            prefs.edit().putString("favorite_artists_json", gson.toJson(finalFavoritos)).commit();

                                            // Notificar a FragmentHome SÓLO DESPUÉS de guardar
                                            sendBroadcast(new Intent("ARTIST_FAVORITE_UPDATE"));
                                        } else {
                                            Log.w(TAG, "⚠️ No se encontró el artista para actualizar en la lista después de la búsqueda.");
                                        }

                                    } else {
                                        Log.w(TAG, "⚠️ El artista '" + artista.getNombre() + "' no tiene imágenes en la respuesta de Spotify.");
                                        // Si no hay imagen, aún así guardamos y notificamos para que aparezca sin imagen
                                        prefs.edit().putString("favorite_artists_json", gson.toJson(finalFavoritos)).commit();
                                        sendBroadcast(new Intent("ARTIST_FAVORITE_UPDATE"));
                                    }

                                } else {
                                    Log.e(TAG, "Spotify error body: " + response.errorBody());
                                    // Si falla la búsqueda, aún así guardamos y notificamos
                                    prefs.edit().putString("favorite_artists_json", gson.toJson(finalFavoritos)).commit();
                                    sendBroadcast(new Intent("ARTIST_FAVORITE_UPDATE"));
                                }
                            }

                            @Override
                            public void onFailure(retrofit2.Call<SpotifyArtistSearchResponse> call, Throwable t) {
                                Log.e(TAG, "❌ Fallo en la llamada a la API de búsqueda de Spotify para '" + artista.getNombre() + "': " + t.getMessage(), t);
                                // Si falla la API, aún así guardamos y notificamos
                                prefs.edit().putString("favorite_artists_json", gson.toJson(finalFavoritos)).commit();
                                sendBroadcast(new Intent("ARTIST_FAVORITE_UPDATE"));
                            }
                        });
            } else {
                // Si no hay token o la imagen ya está presente (caso raro aquí)
                // Guardar el artista sin buscar la imagen si no es necesario
                prefs.edit().putString("favorite_artists_json", gson.toJson(favoritos)).commit();
                sendBroadcast(new Intent("ARTIST_FAVORITE_UPDATE"));
            }
        }
    }

    private void toggleFavoriteAlbum(AlbumFavorito album) {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        Gson gson = new Gson();

        // 1. Leer lista actual de álbumes
        String json = prefs.getString("favorite_albums_json", "[]"); // 💡 CLAVE NUEVA
        Type type = new TypeToken<List<AlbumFavorito>>() {}.getType();
        List<AlbumFavorito> favoritos = gson.fromJson(json, type);
        if (favoritos == null) favoritos = new ArrayList<>();

        // 2. Buscar si ya está
        AlbumFavorito existingAlbum = null;
        for (AlbumFavorito a : favoritos) {
            if (a.getSpotifyUri().equals(album.getSpotifyUri())) {
                existingAlbum = a;
                break;
            }
        }

        if (existingAlbum != null) {
            // El álbum existe, lo eliminamos.
            favoritos.remove(existingAlbum);
            Toast.makeText(this, existingAlbum.getAlbumName() + " eliminado de álbumes favoritos", Toast.LENGTH_SHORT).show();

            // 💡 Acción: Guardar cambios y notificar
            prefs.edit().putString("favorite_albums_json", gson.toJson(favoritos)).apply();
            sendBroadcast(new Intent("ALBUM_FAVORITE_UPDATE"));
            return; // Terminar aquí si se eliminó
        } else {
            // No existe, lo agregamos.
            favoritos.add(album);
            Toast.makeText(this, album.getAlbumName() + " agregado a álbumes favoritos", Toast.LENGTH_SHORT).show();

            // 🚀 BÚSQUEDA DE IMAGEN REAL DEL ÁLBUM
            if (MainActivity.spotifyAccessToken != null && !album.getSpotifyId().isEmpty()) {

                SpotifyService api = RetrofitClient.getClient().create(SpotifyService.class);
                List<AlbumFavorito> finalFavoritos = favoritos; // Referencia final para el callback

                api.getAlbumDetails("Bearer " + MainActivity.spotifyAccessToken, album.getSpotifyId()) // 💡 NUEVA LLAMADA
                        .enqueue(new Callback<SpotifyAlbumDetailsResponse>() {
                            @Override
                            public void onResponse(Call<SpotifyAlbumDetailsResponse> call,
                                                   Response<SpotifyAlbumDetailsResponse> response) {
                                if (response.isSuccessful() && response.body() != null
                                        && response.body().getImages() != null
                                        && !response.body().getImages().isEmpty()) {

                                    // Obtener la URL de la imagen de mayor calidad (la primera en la lista)
                                    String imageUrl = response.body().getImages().get(0).getUrl();
                                    album.setImageUrl(imageUrl); // 💡 Actualizar el objeto en la lista

                                    // Guardar la lista actualizada DESPUÉS de obtener la URL
                                    prefs.edit().putString("favorite_albums_json", gson.toJson(finalFavoritos)).apply();

                                    // Notificar a FragmentFavourite para refrescar RecyclerView
                                    Intent intent = new Intent("ALBUM_FAVORITE_UPDATE");
                                    sendBroadcast(intent);
                                    Log.d(TAG, "✅ API Success! Found image URL: " + imageUrl);
                                } else {
                                    Log.e(TAG, "❌ Error API: No se encontraron imágenes para el álbum ID: " + album.getSpotifyId());
                                    // Guardar la lista sin imagen si falla la API
                                    prefs.edit().putString("favorite_albums_json", gson.toJson(finalFavoritos)).apply();
                                    Intent intent = new Intent("ALBUM_FAVORITE_UPDATE");
                                    sendBroadcast(intent);
                                    Log.e(TAG, "❌ API Failed. Response code: " + response.code() + ", Body: " + (response.body() == null ? "NULL" : "Empty"));
                                }
                            }

                            @Override
                            public void onFailure(Call<SpotifyAlbumDetailsResponse> call, Throwable t) {
                                Log.e(TAG, "❌ Error de red al obtener imagen del álbum: " + album.getAlbumName() + " → " + t.getMessage());
                                // Guardar la lista incluso si la red falla
                                prefs.edit().putString("favorite_albums_json", gson.toJson(finalFavoritos)).apply();
                                Intent intent = new Intent("ALBUM_FAVORITE_UPDATE");
                                sendBroadcast(intent);
                                Log.e(TAG, "❌ Network Failure: " + t.getMessage());
                            }
                        });
            } else {
                // Si el token es nulo o el ID es vacío (raro), simplemente guardar la lista sin hacer la búsqueda
                prefs.edit().putString("favorite_albums_json", gson.toJson(favoritos)).apply();
                sendBroadcast(new Intent("ALBUM_FAVORITE_UPDATE"));
            }
        }
    }

    /**
     * Método auxiliar para notificar a FragmentHome que debe recargar la lista de favoritos.
     */
    private void notifyFragmentHomeOfUpdate() {
        Intent intent = new Intent("ARTIST_FAVORITE_UPDATE");
        sendBroadcast(intent);
    }

}
