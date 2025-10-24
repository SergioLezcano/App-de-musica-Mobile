package com.example.appmusic_basico;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;
import android.os.Looper;

import java.util.Locale;


public class ThirdActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private ImageButton playPauseButton;
    private boolean isPlaying = false;
    private static final int SONG_RESOURCE = R.raw.dua_lipa_don_t_start_now;
    private int playbackPosition;

    private SeekBar seekBar;
    private TextView tvCurrentTime; // Para mostrar el tiempo actual (ej: 00:15)
    private TextView tvTotalTime;   // Para mostrar el tiempo total (ej: 03:45)

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.music_play);

        // 1. Inicializar MediaPlayer y Vistas
        mediaPlayer = MediaPlayer.create(this, R.raw.dua_lipa_don_t_start_now);

        playPauseButton = findViewById(R.id.ib_play_pause_icon);
        ImageView minimizar = findViewById(R.id.iv_chevron_down);
        ImageButton btnMore_vert = findViewById(R.id.iv_more_vertical);

        ImageButton skipPreviousButton = findViewById(R.id.ib_skip_anterior);
        ImageButton skipNextButton = findViewById(R.id.ib_skip_next);
        ImageButton shuffleButton = findViewById(R.id.ib_aleatorio);
        ImageButton repeatButton = findViewById(R.id.ib_repeat);

        seekBar = findViewById(R.id.sk_seek_bar);
        tvCurrentTime = findViewById(R.id.tv_current_time);
        tvTotalTime = findViewById(R.id.tv_total_time);

        // 2. Configurar Listeners para los Botones de Control

        // Botón de Reproducir/Pausar
        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePlayPause();
            }
        });

        // Botón de Canción Anterior (Ejemplo)
        skipPreviousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ThirdActivity.this, "Canción Anterior (no implementado)", Toast.LENGTH_SHORT).show();
                // Lógica real: detener el actual, cargar el anterior y reproducir
            }
        });

        // Botón de Siguiente Canción (Ejemplo)
        skipNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ThirdActivity.this, "Siguiente Canción (no implementado)", Toast.LENGTH_SHORT).show();
                // Lógica real: detener el actual, cargar el siguiente y reproducir
            }
        });

        // Botón de Aleatorio (Ejemplo)
        shuffleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ThirdActivity.this, "Reproducción Aleatoria (no implementado)", Toast.LENGTH_SHORT).show();
            }
        });

        // Botón de Repetir (Ejemplo)
        repeatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Alternar el modo de repetición
                boolean willRepeat = !mediaPlayer.isLooping(); //Retorna un valor booleano (boolean) que indica si la reproducción
                // del archivo multimedia actual está configurada para repetirse (looping)
                mediaPlayer.setLooping(willRepeat);
                String message = willRepeat ? "Repetir: Activado" : "Repetir: Desactivado";
                Toast.makeText(ThirdActivity.this, message, Toast.LENGTH_SHORT).show();
                // Aquí podrías cambiar el drawable para reflejar el estado de repetición
            }
        });

        // Configurar el Listener del SeekBar para permitir buscar (drag)
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Si el usuario desliza el SeekBar, mueve la reproducción
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Esto le indica a la MainActivity que debe mostrar el mini-reproductor al reanudarse.
        MainActivity.isMusicPlaying = true;

        minimizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lógica de minimizado: La música ya está sonando, solo navega
                MainActivity.isMusicPlaying = isPlaying; // Sincroniza el estado
                Intent intent = new Intent(ThirdActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        //menu opciones
        btnMore_vert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(v); // Llama a la función para mostrar el menú
            }
        });

        // Listener para cuando el audio termine (Opcional, útil para la reproducción en cola)
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // La canción ha terminado, podrías pasar a la siguiente
                isPlaying = false;
                playPauseButton.setImageResource(R.drawable.play_arrow_24dp); // Cambia el icono a 'Play'
                Toast.makeText(ThirdActivity.this, "Reproducción terminada", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Inicializa el MediaPlayer aquí si no está creado (manejo de rotación/recreación)
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, SONG_RESOURCE);
            isPlaying = false; // Asume pausa al inicio o si se recrea
        }

        // Sincroniza el icono con el estado de la reproducción
        if (mediaPlayer.isPlaying()) {
            isPlaying = true;
            playPauseButton.setImageResource(R.drawable.pause_24dp);
        } else {
            isPlaying = false;
            playPauseButton.setImageResource(R.drawable.play_arrow_24dp);
        }

        // 🔥 Llama a la configuración del SeekBar y a la actualización
        prepareSeekBar();
        initializeSeekBarUpdate();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Detener las actualizaciones del Handler cuando la actividad no es visible
        handler.removeCallbacks(updateSeekBar);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateSeekBar);
        // Libera los recursos del MediaPlayer cuando la actividad se destruya.
        // Evita fugas de memoria y un uso ineficiente de la batería.
        if (mediaPlayer != null) {
            mediaPlayer.release(); // Libera los recursos
            mediaPlayer = null;
        }
    }

    // Método central para controlar la reproducción/pausa
    // El método togglePlayPause gestiona el cambio de estado (Reproducir ⏯️ y Pausar ⏸️)
    private void togglePlayPause() {
        if (mediaPlayer == null) return; // Seguridad

        if (mediaPlayer.isPlaying()) {   //Esto pregunta directamente al objeto MediaPlayer si está activo,
            // Si Está reproduciendo, pausar
            playbackPosition = mediaPlayer.getCurrentPosition();
            mediaPlayer.pause();
            isPlaying = false;
            playPauseButton.setImageResource(R.drawable.play_arrow_24dp); // Cambia al Icono de Reproducir
            Toast.makeText(this, "Pausa (ms = " + mediaPlayer.getCurrentPosition() + ")", Toast.LENGTH_SHORT).show();
        } else {
            // Si Está pausado o detenido, reproducir
            mediaPlayer.seekTo(playbackPosition);
            mediaPlayer.start();
            isPlaying = true;
            playPauseButton.setImageResource(R.drawable.pause_24dp); // Cambia al Icono de Pausa
            Toast.makeText(this, "Reproduciendo (ms = " + mediaPlayer.getCurrentPosition() + ")", Toast.LENGTH_SHORT).show();
        }

        // Sincroniza el estado global al cambiarlo
        MainActivity.isMusicPlaying = isPlaying;
    }

    // Agrega este método a ThirdActivity para configurar el tiempo total
    private void prepareSeekBar() {
        if (mediaPlayer != null) {
            // 1. Establecer el máximo del SeekBar (duración total en milisegundos)
            int duration = mediaPlayer.getDuration();
            seekBar.setMax(duration);

            // 2. Mostrar la duración total formateada
            tvTotalTime.setText(formatTime(duration));
        }
    }


    // Crea este Runnable para la actualización periódica
    private void initializeSeekBarUpdate() {
        updateSeekBar = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    // Obtener la posición actual
                    int currentPosition = mediaPlayer.getCurrentPosition();

                    // 1. Actualizar el progreso del SeekBar
                    seekBar.setProgress(currentPosition);

                    // 2. Actualizar el TextView del tiempo actual
                    tvCurrentTime.setText(formatTime(currentPosition));

                    // 3. Programar la próxima ejecución en 50 ms (para una actualización fluida)
                    handler.postDelayed(this, 50);
                } else {
                    // Si está en pausa o detenido, aún necesitamos programar la siguiente
                    // llamada para que reanude la actualización si se toca 'Play'
                    handler.postDelayed(this, 50);
                }
            }
        };
        // Inicia el Runnable
        handler.post(updateSeekBar);
    }

    // Método para formatear el tiempo (MM:SS)
    private String formatTime(int milliseconds) {
        long totalSeconds = milliseconds / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        // Formato con ceros iniciales (ej: 03:05)
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    // Función central para manejar la lógica de mostrar y seleccionar el menú
    private void showPopupMenu(View view) {
        // 1. Crear una instancia de PopupMenu, anclándola al View (el ImageButton)
        PopupMenu popup = new PopupMenu(this, view);

        // 2. Inflar el menú (cargar las opciones del XML)
        popup.getMenuInflater().inflate(R.menu.menu_opciones_more_vert, popup.getMenu());

        // 3. Asignar el Listener para manejar la selección de opciones
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Lógica para manejar la acción
                return handleMenuItemSelection(item);
            }
        });

        // 4. Mostrar el menú
        popup.show();

    }
    //Metodo para manejar la Selección (Función de Acción)
    private boolean handleMenuItemSelection(MenuItem item) {
        int id = item.getItemId();

        // Uso de if/else para la lógica de selección
        if (id == R.id.opcion_favoritos) {
            Toast.makeText(this, "Acción: agregar a favoritos", Toast.LENGTH_SHORT).show();
            // Lógica para agregar a favoritos
            return true;
        } else if (id == R.id.opcion_play_list) {
            Toast.makeText(this, "Acción: agregar a playlist", Toast.LENGTH_SHORT).show();
            // Lógica para agregar a la playlist
            return true;
        } else if (id == R.id.opcion_fila) {
            Toast.makeText(this, "Acción: agregar a la fila", Toast.LENGTH_SHORT).show();
            // Lógica para agregar a la fila de reproduccion
            return true;
        } else if (id == R.id.opcion_album) {
            Toast.makeText(this, "Acción: ver album", Toast.LENGTH_SHORT).show();
            // Lógica para mostrar el album
            return true;
        } else if (id == R.id.opcion_artista) {
            Toast.makeText(this, "Acción: ver artista", Toast.LENGTH_SHORT).show();
            // Lógica para mostrar el/la artista
            return true;
        } else if (id == R.id.opcion_compartir) {
            Toast.makeText(this, "Acción: compartir", Toast.LENGTH_SHORT).show();
            // Lógica para compartir
            return true;
        } else if (id == R.id.opcion_ocultar) {
            Toast.makeText(this, "Acción: ocultar", Toast.LENGTH_SHORT).show();
            // Lógica para ocultar la canción
            return true;
        }

        return false; // Indica que la acción no fue manejada
    }
}
