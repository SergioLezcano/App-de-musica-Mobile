package com.example.appmusic_basico;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import models.Cancion_Reciente;

public class ThirdActivity extends AppCompatActivity {

    private ImageButton playPauseButton, skipNextButton, skipPrevButton;
    private ImageButton minimizeButton, repeatButton, stopButton, moreVertButton;
    private SeekBar seekBar;
    private TextView tvSongTitle;

    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private Runnable seekBarRunnable;

    private boolean isPlaying = false;

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
        tvSongTitle = findViewById(R.id.tv_title_song);

        // --- Listeners ---
        playPauseButton.setOnClickListener(v -> togglePlayPause());
        skipNextButton.setOnClickListener(v -> playNext());
        skipPrevButton.setOnClickListener(v -> playPrev());
        minimizeButton.setOnClickListener(v -> minimize());
        repeatButton.setOnClickListener(v -> toggleRepeat());
        stopButton.setOnClickListener(v -> stopPlayback());
        moreVertButton.setOnClickListener(this::showPopupMenu);

        // --- Recuperar canción seleccionada si viene desde SecondaryActivity ---
        Intent intent = getIntent();
        int songResId = intent.getIntExtra("SONG_RESOURCE_ID", -1);
        if (songResId != -1) {
            MainActivity.currentSongIndex = findSongIndexByResId(songResId);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Usar MediaPlayer global de MainActivity
        mediaPlayer = MainActivity.globalMediaPlayer;

        // Si no hay MediaPlayer, crear uno para la canción actual
        if (mediaPlayer == null && !MainActivity.globalPlaylist.isEmpty()) {
            Cancion_Reciente song = MainActivity.globalPlaylist.get(MainActivity.currentSongIndex);
            mediaPlayer = MediaPlayer.create(this, song.getSongResourceId());
            MainActivity.globalMediaPlayer = mediaPlayer;
        }

        isPlaying = mediaPlayer != null && mediaPlayer.isPlaying();
        setupSeekBar();
        updateUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (seekBarRunnable != null) handler.removeCallbacks(seekBarRunnable);
    }

    // --- Métodos de reproducción ---
    private void togglePlayPause() {
        if (mediaPlayer == null) return;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPlaying = false;
        } else {
            mediaPlayer.start();
            isPlaying = true;
        }
        MainActivity.isMusicPlaying = isPlaying;
        updateUI();
    }

    private void playNext() {
        if (MainActivity.globalPlaylist.isEmpty()) return;
        MainActivity.currentSongIndex = (MainActivity.currentSongIndex + 1) % MainActivity.globalPlaylist.size();
        playSongAtIndex(MainActivity.currentSongIndex);
    }

    private void playPrev() {
        if (MainActivity.globalPlaylist.isEmpty()) return;
        // Si la canción tiene más de 3 seg reproducidos, reiniciar
        if (mediaPlayer != null && mediaPlayer.getCurrentPosition() > 3000) {
            mediaPlayer.seekTo(0);
            return;
        }
        MainActivity.currentSongIndex =
                (MainActivity.currentSongIndex - 1 + MainActivity.globalPlaylist.size()) % MainActivity.globalPlaylist.size();
        playSongAtIndex(MainActivity.currentSongIndex);
    }

    private void playSongAtIndex(int index) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        Cancion_Reciente song = MainActivity.globalPlaylist.get(index);
        mediaPlayer = MediaPlayer.create(this, song.getSongResourceId());
        MainActivity.globalMediaPlayer = mediaPlayer;
        mediaPlayer.start();
        isPlaying = true;
        MainActivity.isMusicPlaying = true;
        setupSeekBar();
        updateUI();
    }

    private int findSongIndexByResId(int resId) {
        for (int i = 0; i < MainActivity.globalPlaylist.size(); i++) {
            if (MainActivity.globalPlaylist.get(i).getSongResourceId() == resId) {
                return i;
            }
        }
        return 0;
    }

    private void toggleRepeat() {
        if (mediaPlayer != null) {
            boolean loop = !mediaPlayer.isLooping();
            mediaPlayer.setLooping(loop);
            Toast.makeText(this, loop ? "Repetir activado" : "Repetir desactivado", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopPlayback() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            MainActivity.globalMediaPlayer = null;
        }
        isPlaying = false;
        MainActivity.isMusicPlaying = false;
        finish();
    }

    private void minimize() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    // --- UI / SeekBar ---
    private void setupSeekBar() {
        if (mediaPlayer == null) return;
        seekBar.setMax(mediaPlayer.getDuration());

        seekBarRunnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                }
                handler.postDelayed(this, 500);
            }
        };
        handler.post(seekBarRunnable);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) mediaPlayer.seekTo(progress);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void updateUI() {
        if (!MainActivity.globalPlaylist.isEmpty()) {
            tvSongTitle.setText(MainActivity.globalPlaylist.get(MainActivity.currentSongIndex).getTitulo());
        }
        playPauseButton.setImageResource(isPlaying ? R.drawable.pause_24dp : R.drawable.play_arrow_24dp);
    }

    // --- Menú opciones ---
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
