package com.example.appmusic_basico;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.spotify.android.appremote.api.SpotifyAppRemote;

public class SecondaryActivity extends AppCompatActivity {

    private SpotifyAppRemote mSpotifyAppRemote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.music_list);

        // --- Views ---
        ImageButton backButton = findViewById(R.id.btn_back);
        FrameLayout playButtonLayout = findViewById(R.id.play_button_layout);
        ImageView cover = findViewById(R.id.img_artist);
        TextView title = findViewById(R.id.tv_title_cancion);
        TextView artist = findViewById(R.id.tv_artist_name);

        // --- Intent Data ---
        Intent intent = getIntent();
        int songResourceId;
        String songTitle = "";
        String songArtist = "";
        int songImage = 0;

        if (intent != null) {
            songTitle = intent.getStringExtra("SONG_TITLE");
            songArtist = intent.getStringExtra("SONG_ARTIST");
            songImage = intent.getIntExtra("SONG_IMAGE", 0);
            songResourceId = intent.getIntExtra("SONG_RESOURCE_ID", 0);

            if (title != null) title.setText(songTitle);
            if (artist != null) artist.setText(songArtist);
            if (cover != null && songImage != 0) cover.setImageResource(songImage);
        } else {
            songResourceId = 0;
        }

        // --- Back button ---
        backButton.setOnClickListener(v -> finish());

        // --- Play button ---
        playButtonLayout.setOnClickListener(v -> {
            // Obtener conexión a Spotify
            mSpotifyAppRemote = MainActivity.getSpotifyAppRemote();

            if (mSpotifyAppRemote != null) {
                // Actualizar índice y estado global
                int songIndex = findSongIndexByUri(String.valueOf(songResourceId));
                MainActivity.currentSongIndex = songIndex;
                MainActivity.isMusicPlaying = true;

                // Obtener URI de la canción
                String spotifyUri = MainActivity.playlistUris.get(songIndex);

                // Reproducir la canción con callbacks
                mSpotifyAppRemote.getPlayerApi().play(spotifyUri)
                        .setResultCallback(empty -> {
                            Toast.makeText(this, "Reproduciendo: " + title, Toast.LENGTH_SHORT).show();
                            // Abrir ThirdActivity solo después de que la reproducción inicia
                            Intent playIntent = new Intent(SecondaryActivity.this, ThirdActivity.class);
                            startActivity(playIntent);
                        })
                        .setErrorCallback(error -> {
                            Toast.makeText(this, "Error al reproducir canción", Toast.LENGTH_SHORT).show();
                            android.util.Log.e("SecondaryActivity", "Error al reproducir: " + String.valueOf(error));
                        });

            } else {
                Toast.makeText(this, "Spotify no conectado", Toast.LENGTH_SHORT).show();
            }
        });

        // --- Menú opciones ---
        int[] moreButtons = {R.id.iv_more_vertical_1, R.id.iv_more_vertical_2, R.id.iv_more_vertical_3};
        for (int btnId : moreButtons) {
            ImageButton btn = findViewById(btnId);
            btn.setOnClickListener(this::showPopupMenu);
        }
    }

    // Encuentra la posición de la canción en la playlist global
    private int findSongIndexByUri(String spotifyUri) {
        for (int i = 0; i < MainActivity.globalPlaylist.size(); i++) {
            if (spotifyUri.equals(MainActivity.globalPlaylist.get(i).getSpotifyUri())) {
                return i;
            }
        }
        return 0;
    }


    // Menú Popup centralizado
    private void showPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenuInflater().inflate(R.menu.menu_opciones_music_list, popup.getMenu());
        popup.setOnMenuItemClickListener(this::handleMenuItemSelection);
        popup.show();
    }

    private boolean handleMenuItemSelection(MenuItem item) {
        int id = item.getItemId();
        String msg = "";

        if (id == R.id.opcion_reproducir) msg = "Reproducir música";
        else if (id == R.id.opcion_agregar_favoritos) msg = "Agregar a favoritos";
        else if (id == R.id.opcion_agregar_a_lista) msg = "Agregar a la lista";
        else if (id == R.id.opcion_ocultar) msg = "Ocultar";
        else return false;

        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        return true;
    }


}




