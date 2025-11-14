package com.example.appmusic_basico;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appmusic_basico.api.RetrofitClient;
import com.example.appmusic_basico.api.SpotifyService;
import com.example.appmusic_basico.api.SpotifyAlbumTracksResponse;
import com.bumptech.glide.Glide;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.spotify.android.appremote.api.SpotifyAppRemote;

import java.util.ArrayList;
import java.util.List;

import adapters.AlbumDetalleAdapter;
import models.Cancion_Reciente;

public class AlbumDetalleActivity extends AppCompatActivity implements AlbumDetalleAdapter.AlbumClickListener {

    private static final String TAG = "AlbumDetalleActivity";
    private RecyclerView rvCancionesAlbum;
    private AlbumDetalleAdapter albumDetalleAdapter;
    private List<Cancion_Reciente> topTrackAlbum = new ArrayList<>();

    // Variables para los datos del álbum
    private String albumId;
    private String albumName;
    private String artistName;
    private String albumImageUrl;
    private String albumUri;

    // Vistas de la cabecera
    private ImageView ivPortada;
    private TextView tvTituloAlbum;
    private TextView tvNombreArtista;
    private ImageButton btnVolver, btn_bigPlay;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_detalle);

        View miniPlayer = findViewById(R.id.mini_player_bar);
        TextView miniTitle = findViewById(R.id.mini_player_track_title);
        ImageButton miniPlayPause = findViewById(R.id.mini_player_play_pause);

        // Inicializar Vistas
        ivPortada = findViewById(R.id.iv_album_detalle);
        tvTituloAlbum = findViewById(R.id.tv_album_nombre);
        tvNombreArtista = findViewById(R.id.tv_artista_nombre);
        btnVolver = findViewById(R.id.iv_btnVolver);
        rvCancionesAlbum = findViewById(R.id.rv_contenedor_canciones_album);
        btn_bigPlay = findViewById(R.id.ib_btnPlay_Pause);

        // Manejar el botón de volver
        if (btnVolver != null) {
            btnVolver.setOnClickListener(v -> finish());
        }

        // 1. Recibir datos del Intent y actualizar UI de cabecera
        getIntentData();

        // Suscripción al estado del player (Mini Player y Botón Play Grande)
        SpotifyAppRemote remote = MainActivity.getSpotifyAppRemote();

        if (remote != null) {
            remote.getPlayerApi()
                    .subscribeToPlayerState()
                    .setEventCallback(playerState -> {

                        boolean isPaused = playerState.isPaused;

                        // Sincronizar botón grande
                        btn_bigPlay.setImageResource(
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

        // Botón Play/Pause del Mini Player
        miniPlayPause.setOnClickListener(v -> {
            SpotifyAppRemote r = MainActivity.getSpotifyAppRemote();
            if (r != null) {
                r.getPlayerApi().getPlayerState().setResultCallback(state -> {
                    if (state.isPaused) r.getPlayerApi().resume();
                    else r.getPlayerApi().pause();
                });
            }
        });

        // Click en Mini Player → abrir ThirdActivity
        miniPlayer.setOnClickListener(v -> {
            Intent i = new Intent(this, ThirdActivity.class);
            startActivity(i);
        });

        // Botón Play/Pause Grande
        btn_bigPlay.setOnClickListener(v -> {
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

        // 2. Configurar RecyclerView y Adaptador
        rvCancionesAlbum.setLayoutManager(new LinearLayoutManager(this));
        albumDetalleAdapter = new AlbumDetalleAdapter(topTrackAlbum, this); // 'this' implementa AlbumClickListener
        rvCancionesAlbum.setAdapter(albumDetalleAdapter);


        // 3. Cargar las canciones del álbum
        if (albumId != null) {
            loadAlbumTracks();
        } else {
            Toast.makeText(this, "Error: ID de álbum no encontrado.", Toast.LENGTH_LONG).show();
        }
    }

    // ----------------------------------------------------
    // 🎶 Implementación de AlbumClickListener (Reproducción)
    // ----------------------------------------------------
    @Override
    public void onTrackClick(Cancion_Reciente track) {
        SpotifyAppRemote remote = MainActivity.getSpotifyAppRemote();

        if (remote != null) {
            // Iniciar la reproducción con la URI del track.
            remote.getPlayerApi().play(track.getSpotifyUri())
                    .setResultCallback(empty -> {
                        Toast.makeText(this, "Reproduciendo: " + track.getTitulo(), Toast.LENGTH_SHORT).show();
                    })
                    .setErrorCallback(error -> {
                        Log.e(TAG, "Error al reproducir track: " + error.getMessage());
                        Toast.makeText(this, "Fallo al iniciar reproducción.", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "Spotify App Remote no está conectado.", Toast.LENGTH_SHORT).show();
        }
    }


    // ----------------------------------------------------
    // 🌐 Lógica de Datos y API
    // ----------------------------------------------------
    private void getIntentData() {
        Intent intent = getIntent();
        albumId = intent.getStringExtra("ALBUM_ID");
        albumName = intent.getStringExtra("ALBUM_NAME");
        artistName = intent.getStringExtra("ARTIST_NAME");
        albumImageUrl = intent.getStringExtra("ALBUM_IMAGE_URL");
        albumUri = intent.getStringExtra("ALBUM_URI");

        // Actualizar UI de la cabecera con Glide
        if (albumName != null) tvTituloAlbum.setText(albumName);
        if (artistName != null) tvNombreArtista.setText(artistName);
        if (albumImageUrl != null) {
            Glide.with(this).load(albumImageUrl).into(ivPortada);
        }
    }

    private void loadAlbumTracks() {
        if (MainActivity.spotifyAccessToken == null) {
            Toast.makeText(this, "Spotify no conectado.", Toast.LENGTH_SHORT).show();
            return;
        }

        SpotifyService api = RetrofitClient.getClient().create(SpotifyService.class);

        api.getAlbumTracks(
                "Bearer " + MainActivity.spotifyAccessToken,
                albumId
        ).enqueue(new Callback<SpotifyAlbumTracksResponse>() {
            @Override
            public void onResponse(Call<SpotifyAlbumTracksResponse> call, Response<SpotifyAlbumTracksResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    processAlbumTracks(response.body());
                } else {
                    Log.e(TAG, "Error API Tracks: " + response.code());
                    Toast.makeText(AlbumDetalleActivity.this, "Error al cargar canciones.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SpotifyAlbumTracksResponse> call, Throwable t) {
                Log.e(TAG, "Fallo de red al obtener tracks: " + t.getMessage());
            }
        });
    }

    private void processAlbumTracks(SpotifyAlbumTracksResponse response) {
        topTrackAlbum.clear();

        if (response.getItems() != null) {
            for (SpotifyAlbumTracksResponse.Item trackItem : response.getItems()) {
                // Mapeamos la pista del álbum a nuestro modelo Cancion_Reciente
                topTrackAlbum.add(new Cancion_Reciente(
                        trackItem.getName(),
                        artistName,         // Usamos el nombre del artista del álbum
                        albumImageUrl,      // Usamos la carátula del álbum
                        trackItem.getUri(), // URI de la pista
                        true                // fromSpotify
                ));
            }
            albumDetalleAdapter.notifyDataSetChanged();
        }
    }
}