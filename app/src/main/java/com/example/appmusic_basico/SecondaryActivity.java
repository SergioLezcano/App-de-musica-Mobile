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

        // Mini Player UI
        View miniPlayer = findViewById(R.id.mini_player_bar);
        TextView miniTitle = findViewById(R.id.mini_player_track_title);
        ImageButton miniPlayPause = findViewById(R.id.mini_player_play_pause);
        ImageView bigPlayButton = findViewById(R.id.ic_play_2);
        ImageView shuffleButton = findViewById(R.id.iv_aleatorio);

        // Suscripción al estado del player (igual que MainActivity)
        SpotifyAppRemote remote = MainActivity.getSpotifyAppRemote();

        if (remote != null) {
            remote.getPlayerApi()
                    .subscribeToPlayerState()
                    .setEventCallback(playerState -> {

                        boolean isPaused = playerState.isPaused;

                        // ✅ Sincronizar botón grande
                        bigPlayButton.setImageResource(
                                isPaused ? R.drawable.play_arrow_24dp : R.drawable.pause_24dp
                        );

                        if (playerState.track != null) {
                            miniTitle.setText(playerState.track.name + " - " + playerState.track.artist.name);
                            miniPlayer.setVisibility(View.VISIBLE);

                            miniPlayPause.setImageResource(
                                    playerState.isPaused ? R.drawable.play_arrow_24dp : R.drawable.pause_24dp
                            );
                        }
                    });
        }

        // Botón Play/Pause
        miniPlayPause.setOnClickListener(v -> {
            SpotifyAppRemote r = MainActivity.getSpotifyAppRemote();
            if (r != null) {
                r.getPlayerApi().getPlayerState().setResultCallback(state -> {
                    if (state.isPaused) r.getPlayerApi().resume();
                    else r.getPlayerApi().pause();
                });
            }
        });

        // Click → abrir ThirdActivity
        miniPlayer.setOnClickListener(v -> {
            Intent i = new Intent(this, ThirdActivity.class);
            startActivity(i);
        });

        bigPlayButton.setOnClickListener(v -> {
            SpotifyAppRemote r = MainActivity.getSpotifyAppRemote();
            if (r == null) return;

            r.getPlayerApi().getPlayerState().setResultCallback(state -> {
                if (state.isPaused) {
                    r.getPlayerApi().resume();
                } else {
                    r.getPlayerApi().pause();
                }
            });
        });

        //Listener de btn aleatorio
        shuffleButton.setOnClickListener(v -> playRandomTrack());

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

        String safeImage = (artistImage == null || artistImage.isEmpty())
                ? null
                : artistImage;
        Log.e("SECONDARY", "Artist ID = " + artistId);
        Log.e("SECONDARY", "Artist Name = " + artistName);
        Log.e("SECONDARY", "Artist Image = " + artistImage);


        Glide.with(this)
                .load(safeImage)
                .placeholder(R.drawable.image_2930)
                .error(R.drawable.image_2930)
                .into(imgArtist);

        // Botón volver
        backButton.setOnClickListener(v -> finish());

        // Botón Play (opcional: reproducir primera canción del artista)
        playButtonLayout.setOnClickListener(v -> {
            if (!topTracks.isEmpty()) {
                playSong(topTracks.get(0));

            }
        });

        // Cargar top tracks desde Spotify API
        loadArtistTopTracks();

        showMiniPlayer();
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
        playSong(cancion);
    }

    // ===========================================================================================
    // ✅ REPRODUCIR UNA CANCION USANDO MAINACTIVITY (PLAYLISTMANAGER)
    // ===========================================================================================
    private void playSong(Cancion_Reciente c) {

        if (c == null) return;

        SpotifyAppRemote remote = MainActivity.getSpotifyAppRemote();
        if (remote == null) {
            Toast.makeText(this, "Spotify no conectado", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Agregar a recientes automáticamente
        addToLocalRecents(c);

        // ✅ Reproducir
        MainActivity.playlistManager.playUri(c.getSpotifyUri());
        showMiniPlayer();
    }


    private void showMiniPlayer() {
        View miniBar = findViewById(R.id.mini_player_bar);
        TextView title = findViewById(R.id.mini_player_track_title);
        ImageButton playPause = findViewById(R.id.mini_player_play_pause);

        miniBar.setVisibility(View.VISIBLE);

        SpotifyAppRemote remote = MainActivity.getSpotifyAppRemote();
        if (remote != null) {
            remote.getPlayerApi().getPlayerState().setResultCallback(state -> {

                if (state.track != null) {
                    title.setText(state.track.name + " - " + state.track.artist.name);

                    playPause.setImageResource(
                            state.isPaused ? R.drawable.play_arrow_24dp : R.drawable.pause_24dp
                    );
                }

            });
        }
    }


    private void playRandomTrack() {

        if (topTracks == null || topTracks.isEmpty()) {
            Toast.makeText(this, "No hay canciones para reproducir.", Toast.LENGTH_SHORT).show();
            return;
        }

        int randomIndex = (int) (Math.random() * topTracks.size());
        Cancion_Reciente randomTrack = topTracks.get(randomIndex);

        String uri = randomTrack.getSpotifyUri();

        SpotifyAppRemote remote = MainActivity.getSpotifyAppRemote();
        if (remote == null) {
            Toast.makeText(this, "Spotify no conectado", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Reproducción usando PlaylistManager
        if (MainActivity.playlistManager != null) {
            MainActivity.playlistManager.playUri(uri);
        } else {
            remote.getPlayerApi().play(uri);
        }

        Toast.makeText(this, "Reproduciendo (aleatorio): " + randomTrack.getTitulo(), Toast.LENGTH_SHORT).show();
    }

    private void addToLocalRecents(Cancion_Reciente c) {

        if (c == null) return;

        // Evitar duplicados por URI
        for (Cancion_Reciente x : MainActivity.globalPlaylist) {
            if (x.getSpotifyUri().equals(c.getSpotifyUri())) {
                return;
            }
        }

        // Insertar al inicio como "reciente"
        MainActivity.globalPlaylist.add(0, c);

        // Enviar broadcast interno → FragmentHome actualizará el Recycler
        Intent intent = new Intent("UPDATE_RECENTLY_PLAYED");
        sendBroadcast(intent);
    }


}




