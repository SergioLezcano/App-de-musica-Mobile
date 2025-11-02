package com.example.appmusic_basico;

import androidx.activity.EdgeToEdge;
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
        EdgeToEdge.enable(this);
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

        // --- Play button: Reproducir usando playlist global ---
        playButtonLayout.setOnClickListener(v -> {
            if (mSpotifyAppRemote == null) {
                mSpotifyAppRemote = MainActivity.getSpotifyAppRemote();
            }

            if (mSpotifyAppRemote != null) {
                // Actualizar playlist global en MainActivity
                MainActivity.currentSongIndex = findSongIndexByResourceId(songResourceId);
                MainActivity.isMusicPlaying = true;

                // Reproducir la canción usando SpotifyAppRemote
                String spotifyUri = MainActivity.playlistUris.get(MainActivity.currentSongIndex);
                mSpotifyAppRemote.getPlayerApi().play(spotifyUri);

                // Abrir ThirdActivity para ver controles
                Intent playIntent = new Intent(SecondaryActivity.this, ThirdActivity.class);
                startActivity(playIntent);
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
    private int findSongIndexByResourceId(int resourceId) {
        for (int i = 0; i < MainActivity.globalPlaylist.size(); i++) {
            if (MainActivity.globalPlaylist.get(i).getSongResourceId() == resourceId) {
                return i;
            }
        }
        return 0; // fallback al primero
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
