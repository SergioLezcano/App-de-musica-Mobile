package com.example.appmusic_basico;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.appmusic_basico.api.RetrofitClient;
import com.example.appmusic_basico.api.SpotifyArtistTopTracksResponse;
import com.example.appmusic_basico.api.SpotifyService;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import java.util.ArrayList;
import java.util.List;

import adapters.SongListAdapter;
import models.Cancion_Reciente;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SecondaryActivity extends AppCompatActivity {

    private SpotifyAppRemote mSpotifyAppRemote;
    private RecyclerView rvSongList;
    private SongListAdapter songListAdapter;

    private ImageView imgArtist;
    private TextView tvArtistName;
    private TextView tvArtistTitle;

    private String artistId;
    private String artistName;
    private String artistImage;

    private List<Cancion_Reciente> topTracks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.music_list);

        // === Obtener vistas ===
        ImageButton backButton = findViewById(R.id.btn_back);
        FrameLayout playButtonLayout = findViewById(R.id.play_button_layout);
        imgArtist = findViewById(R.id.img_artist);
        tvArtistTitle = findViewById(R.id.tv_title_cancion);
        tvArtistName = findViewById(R.id.tv_artist_name);
        rvSongList = findViewById(R.id.cv_container_song_list);

        // === Configurar RecyclerView ===
        rvSongList.setLayoutManager(new LinearLayoutManager(this));
        songListAdapter = new SongListAdapter(topTracks, this::onSongClicked);
        rvSongList.setAdapter(songListAdapter);

        // === Obtener datos enviados desde el fragment o adapter ===
        Intent intent = getIntent();
        if (intent != null) {
            artistId = intent.getStringExtra("ARTIST_ID");
            artistName = intent.getStringExtra("ARTIST_NAME");
            artistImage = intent.getStringExtra("ARTIST_IMAGE");
        }

        // Mostrar datos en UI
        tvArtistName.setText(artistName);
        tvArtistTitle.setText("Top Canciones");

        Glide.with(this)
                .load(artistImage)
                .placeholder(R.drawable.image_2930)
                .into(imgArtist);

        // Botón volver
        backButton.setOnClickListener(v -> finish());

        // Botón Play (opcional: reproducir primera canción del artista)
        playButtonLayout.setOnClickListener(v -> {
            if (!topTracks.isEmpty()) {
                playSong(topTracks.get(0).getSpotifyUri());
            }
        });

        // Cargar top tracks desde Spotify API
        loadArtistTopTracks();
    }

    // ===========================================================================================
    // ✅ CARGAR TOP TRACKS DEL ARTISTA DESDE SPOTIFY WEB API
    // ===========================================================================================
    private void loadArtistTopTracks() {

        if (MainActivity.spotifyAccessToken == null) {
            Toast.makeText(this, "Token no disponible.", Toast.LENGTH_LONG).show();
            return;
        }

        SpotifyService api = RetrofitClient.getClient().create(SpotifyService.class);

        api.getArtistTopTracks(
                "Bearer " + MainActivity.spotifyAccessToken,
                artistId,
                "AR" // país
        ).enqueue(new Callback<SpotifyArtistTopTracksResponse>() {
            @Override
            public void onResponse(Call<SpotifyArtistTopTracksResponse> call,
                                   Response<SpotifyArtistTopTracksResponse> response) {

                if (!response.isSuccessful() || response.body() == null) {
                    Log.e("SecondaryActivity", "Error API Spotify: " + response.code());
                    return;
                }

                topTracks.clear();

                for (SpotifyArtistTopTracksResponse.Track t : response.body().getTracks()) {

                    String image = "";
                    if (t.getAlbum() != null && t.getAlbum().getImages() != null && !t.getAlbum().getImages().isEmpty()) {
                        image = t.getAlbum().getImages().get(0).getUrl();
                    }

                    topTracks.add(new Cancion_Reciente(
                            t.getName(),
                            t.getArtists().get(0).getName(),
                            image,
                            t.getUri(),
                            true
                    ));
                }

                songListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<SpotifyArtistTopTracksResponse> call, Throwable t) {
                Log.e("SecondaryActivity", "Error: " + t.getMessage());
            }
        });
    }

    // ===========================================================================================
    // ✅ AL HACER CLICK EN UNA CANCION
    // ===========================================================================================
    private void onSongClicked(Cancion_Reciente cancion) {
        playSong(cancion.getSpotifyUri());
    }

    // ===========================================================================================
    // ✅ REPRODUCIR UNA CANCION USANDO MAINACTIVITY (PLAYLISTMANAGER)
    // ===========================================================================================
    private void playSong(String uri) {
        mSpotifyAppRemote = MainActivity.getSpotifyAppRemote();

        if (mSpotifyAppRemote == null) {
            Toast.makeText(this, "Spotify no conectado", Toast.LENGTH_SHORT).show();
            return;
        }

        MainActivity.playlistManager.playUri(uri);

        // Abrir ThirdActivity
        Intent playIntent = new Intent(this, ThirdActivity.class);
        startActivity(playIntent);
    }
}




