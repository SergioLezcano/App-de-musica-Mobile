package com.example.appmusic_basico;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appmusic_basico.api.RetrofitClient;
import com.example.appmusic_basico.api.SpotifyArtistSearchResponse;
import com.example.appmusic_basico.api.SpotifyService;
import com.example.appmusic_basico.api.SpotifyAlbumTracksResponse;
import com.bumptech.glide.Glide;

import models.Artistas;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import java.lang.reflect.Type;
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
    private Cancion_Reciente selectedTrack;
    private final Gson gson = new Gson();
    private String artistSpotifyId;

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
        artistSpotifyId = intent.getStringExtra("ARTIST_ID");
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

    private void toggleFavoriteSong(Cancion_Reciente song) {
        if (song == null) return;

        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        // Usaremos la Gson existente

        // 1. Cargar lista actual
        String json = prefs.getString("favorite_songs_json", "[]");
        Type type = new TypeToken<List<Cancion_Reciente>>() {}.getType();
        List<Cancion_Reciente> favorites = new Gson().fromJson(json, type);
        if (favorites == null) favorites = new ArrayList<>();

        // 2. Comprobar si existe (usando URI como identificador único)
        Cancion_Reciente existingSong = null;
        for (Cancion_Reciente c : favorites) {
            if (c.getSpotifyUri().equals(song.getSpotifyUri())) {
                existingSong = c;
                break;
            }
        }

        // 3. Agregar o Eliminar
        if (existingSong != null) {
            // Eliminar
            favorites.remove(existingSong);
            Toast.makeText(this, song.getTitulo() + " eliminado de favoritos.", Toast.LENGTH_SHORT).show();
        } else {
            // Agregar
            favorites.add(0, song); // Agregar al inicio de la lista
            Toast.makeText(this, song.getTitulo() + " agregado a favoritos.", Toast.LENGTH_SHORT).show();
        }

        // 4. Guardar y Notificar (usando commit para sincronización)
        prefs.edit().putString("favorite_songs_json", new Gson().toJson(favorites)).commit();

        // 5. Enviar Broadcast
        Intent intent = new Intent("SONG_FAVORITE_UPDATE"); // Usar la nueva acción
        sendBroadcast(intent);
    }

    private void showPopupMenu(View view) {
        if (selectedTrack == null) {
            Toast.makeText(this, "No hay canción seleccionada.", Toast.LENGTH_SHORT).show();
            return;
        }

        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenuInflater().inflate(R.menu.menu_opciones_album_detalle, popup.getMenu());
        popup.setOnMenuItemClickListener(this::handleMenuItemSelection);
        popup.show();
    }

    /**
     * Maneja la selección de elementos en el menú de opciones de la canción.
     */
    private boolean handleMenuItemSelection(MenuItem item) {
        if (selectedTrack == null) return false;

        String msg;
        int id = item.getItemId();

        // 1. Agregar a Favoritos de FragmentFavourite(Canción)
        if (id == R.id.op_agregar_musica_favortita) { // Usar un ID específico para canción
            toggleFavoriteSong(selectedTrack);
            return true;
        }
        else if (id == R.id.opcion_agregar_artist_favoritos) {
            if (selectedTrack == null || artistSpotifyId == null) return false;

            String artistName = selectedTrack.getArtistaName();
            String artistImageUrl = selectedTrack.getCoverUrl();

            Artistas newFavorite = new Artistas(artistName, artistImageUrl, artistSpotifyId);
            toggleFavoriteArtist(newFavorite);
            return true;
        }

        else if (id == R.id.opcion_agregar_a_fila) msg = "agreado a la fila" + selectedTrack.getTitulo();
        else if (id == R.id.opcion_compartir_cancion) msg = "Compartir " + selectedTrack.getTitulo();
        else if (id == R.id.opcion_ocultar) msg = "Ocultar " + selectedTrack.getTitulo();
        else return false;

        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public void onMoreOptionsClick(Cancion_Reciente track, View view) {
        // 1. Guardar la canción seleccionada
        this.selectedTrack = track;
        // 2. Mostrar el menú
        showPopupMenu(view);
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
            if (MainActivity.spotifyAccessToken == null) {
                Toast.makeText(this, "Spotify no conectado.", Toast.LENGTH_SHORT).show();
                return;
            }
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

}