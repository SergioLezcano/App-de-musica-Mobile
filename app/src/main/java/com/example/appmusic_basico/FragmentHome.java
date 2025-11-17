package com.example.appmusic_basico;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appmusic_basico.api.RetrofitClient;
import com.example.appmusic_basico.api.SpotifyArtistSearchResponse;
import com.example.appmusic_basico.api.SpotifyRecentlyPlayedResponse;
import com.example.appmusic_basico.api.SpotifyService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import adapters.ArtistAdapter;
import adapters.RecentlyPlayedAdapter;
import managers.PlaylistManager;
import models.Artistas;
import models.Cancion_Reciente;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentHome extends Fragment {

    private static final String TAG = "FragmentHome";

    private RecyclerView recyclerView;
    private RecentlyPlayedAdapter adapter;
    private List<Cancion_Reciente> cancionesRecientes = new ArrayList<>();
    private TextView tvSpotifyStatus;

    private RecyclerView rvArtists;
    private ArtistAdapter artistAdapter;
    private List<Artistas> favoriteArtists = new ArrayList<>();

    private final Gson gson = new Gson();
    private BroadcastReceiver favoriteUpdateReceiver;

    // ===========================================================
    // 🧩 CICLO DE VIDA
    // ===========================================================

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.home_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --------------------------------------------
        // 🎵 Canciones Recientes
        // --------------------------------------------
        recyclerView = view.findViewById(R.id.rv_recently_played);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        tvSpotifyStatus = view.findViewById(R.id.tv_spotify_status);

        adapter = new RecentlyPlayedAdapter(cancionesRecientes, cancion -> {
            if (MainActivity.playlistManager != null) {
                MainActivity.playlistManager.playUri(cancion.getSpotifyUri());
            } else {
                Toast.makeText(getContext(), "Playlist no inicializada. Intentando reproducir...", Toast.LENGTH_LONG).show();
                ((MainActivity) requireActivity()).playSpotifyUri(cancion.getSpotifyUri());
            }
        });
        recyclerView.setAdapter(adapter);

        if (MainActivity.spotifyAccessToken != null) {
            cargarCancionesRecientes();
        } else {
            Log.w(TAG, "Token Spotify no disponible.");
            tvSpotifyStatus.setText("❌ Token Spotify no disponible.");
        }

        // --------------------------------------------
        // 💫 Artistas Favoritos
        // --------------------------------------------
        rvArtists = view.findViewById(R.id.rv_favorite_artists);
        setupFavoriteArtistsRecyclerView(view);

        // --------------------------------------------
        // 📡 Broadcast interno para actualizar artistas
        // --------------------------------------------
        favoriteUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("ARTIST_FAVORITE_UPDATE".equals(intent.getAction())) {
                    Log.d(TAG, "🎵 Broadcast recibido → actualizando lista de artistas favoritos...");

                    // ✅ Llamar a setup para recargar y notificar
                    setupFavoriteArtistsRecyclerView(requireView());
                }
            }
        };

        IntentFilter filter = new IntentFilter("ARTIST_FAVORITE_UPDATE");

        // ✅ Android 13+ requiere flag de exportación
        ContextCompat.registerReceiver(
                requireContext(),
                favoriteUpdateReceiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
        );

        registerRecentSongsReceiver();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (favoriteUpdateReceiver != null) {
            requireContext().unregisterReceiver(favoriteUpdateReceiver);
            favoriteUpdateReceiver = null;
        }
    }

    // ===========================================================
    // 🎧 CARGA DE CANCIONES RECIENTES
    // ===========================================================
    public void cargarCancionesRecientes() {
        if (MainActivity.spotifyAccessToken == null) return;

        SpotifyService api = RetrofitClient.getClient().create(SpotifyService.class);

        api.getRecentlyPlayed("Bearer " + MainActivity.spotifyAccessToken)
                .enqueue(new Callback<SpotifyRecentlyPlayedResponse>() {
                    @Override
                    public void onResponse(Call<SpotifyRecentlyPlayedResponse> call,
                                           Response<SpotifyRecentlyPlayedResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            cancionesRecientes.clear();
                            MainActivity.globalPlaylist.clear();

                            for (SpotifyRecentlyPlayedResponse.Item item : response.body().getItems()) {
                                String titulo = item.getTrack().getName().trim();
                                String artista = item.getTrack().getArtists().get(0).getName();
                                String spotifyUri = item.getTrack().getUri();

                                String coverUrl = "";
                                if (item.getTrack().getAlbum().getImages() != null &&
                                        !item.getTrack().getAlbum().getImages().isEmpty()) {
                                    coverUrl = item.getTrack().getAlbum().getImages().get(0).getUrl();
                                }

                                Cancion_Reciente cancion = new Cancion_Reciente(
                                        titulo, artista, coverUrl, spotifyUri, true
                                );

                                cancionesRecientes.add(cancion);
                                MainActivity.globalPlaylist.add(cancion);
                            }

                            if (MainActivity.playlistManager == null) {
                                MainActivity.playlistManager = new PlaylistManager(
                                        MainActivity.globalPlaylist,
                                        MainActivity.getSpotifyAppRemote()
                                );
                            } else {
                                MainActivity.playlistManager.setPlaylist(MainActivity.globalPlaylist);
                            }

                            adapter.notifyDataSetChanged();
                            tvSpotifyStatus.setText("✅ Spotify: Datos cargados.");
                            Log.d(TAG, "✅ Canciones recientes cargadas: " + cancionesRecientes.size());
                        } else {
                            Log.e(TAG, "❌ Error API Spotify: " + response.code());
                            tvSpotifyStatus.setText("❌ Spotify: Error API (" + response.code() + ")");
                        }
                    }

                    @Override
                    public void onFailure(Call<SpotifyRecentlyPlayedResponse> call, Throwable t) {
                        Log.e(TAG, "❌ Fallo al cargar canciones recientes: " + t.getMessage(), t);
                        tvSpotifyStatus.setText("❌ Error al cargar canciones.");
                    }
                });
    }

    // ===========================================================
    // 🌟 ARTISTAS FAVORITOS
    // ===========================================================

    private void setupFavoriteArtistsRecyclerView(View view) {
        // 1. Cargar datos frescos de SharedPreferences
        List<Artistas> freshArtists = loadFavoriteArtists();

        // 2. Actualizar la lista interna
        favoriteArtists.clear();
        favoriteArtists.addAll(freshArtists);

        // 3. Controlar la visibilidad
        if (rvArtists != null) {
            rvArtists.setVisibility(favoriteArtists.isEmpty() ? View.GONE : View.VISIBLE);
        }

        // 4. Inicializar o notificar al adaptador
        if (artistAdapter == null) {
            artistAdapter = new ArtistAdapter(favoriteArtists);
            rvArtists.setLayoutManager(
                    new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
            );
            rvArtists.setAdapter(artistAdapter);
        } else {
            artistAdapter.notifyDataSetChanged();
        }
    }

    private List<Artistas> loadFavoriteArtists() {
        if (getContext() == null) return new ArrayList<>();

        android.content.SharedPreferences prefs =
                getContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);

        String jsonFavorites = prefs.getString("favorite_artists_json", "[]");
        Type type = new TypeToken<List<Artistas>>() {}.getType();
        List<Artistas> favorites = gson.fromJson(jsonFavorites, type);

        return favorites != null ? favorites : new ArrayList<>();
    }


    private void registerRecentSongsReceiver() {
        BroadcastReceiver recentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                cancionesRecientes.clear();
                cancionesRecientes.addAll(MainActivity.globalPlaylist);

                adapter.notifyDataSetChanged();
            }
        };

        IntentFilter filter = new IntentFilter("UPDATE_RECENTLY_PLAYED");
        ContextCompat.registerReceiver(
                requireContext(),
                recentReceiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
        );
    }

    // ===========================================================
    // 🧼 MANEJO DE CIERRE DE SESIÓN
    // ===========================================================

    public void clearContentOnLogout() {
        // Limpiar la lista de canciones recientes del adaptador
        cancionesRecientes.clear();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        // Limpiar la lista de artistas favoritos
        favoriteArtists.clear();
        if (artistAdapter != null) {
            artistAdapter.notifyDataSetChanged();
        }
        if (rvArtists != null) {
            rvArtists.setVisibility(View.GONE);
        }

        // Mostrar el estado de desconexión
        if (tvSpotifyStatus != null) {
            tvSpotifyStatus.setText("Sesión cerrada. Autenticando...");
        }

        Log.d(TAG, "Contenido de FragmentHome limpiado tras cerrar sesión.");
    }


}
