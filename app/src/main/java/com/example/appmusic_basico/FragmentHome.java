package com.example.appmusic_basico;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import adapters.RecentlyPlayedAdapter;
import com.example.appmusic_basico.api.SpotifyRecentlyPlayedResponse;
import com.example.appmusic_basico.api.RetrofitClient;
import com.example.appmusic_basico.api.SpotifyService;
import managers.PlaylistManager;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_fragment, container, false);

        recyclerView = view.findViewById(R.id.rv_recently_played);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        tvSpotifyStatus = view.findViewById(R.id.tv_spotify_status);

        // Configurar adapter
        adapter = new RecentlyPlayedAdapter(cancionesRecientes, cancion -> {
            if (MainActivity.playlistManager != null) {

                MainActivity.playlistManager.playUri(cancion.getSpotifyUri());

            } else {
                // Fallback si el manager aún no está listo (aunque esto es menos probable ahora)
                Toast.makeText(getContext(), "Playlist no inicializada. Intentando reproducir directamente.", Toast.LENGTH_LONG).show();
                // Llamar a playSpotifyUri en MainActivity para forzar la reconexión si es necesario
                ((MainActivity) requireActivity()).playSpotifyUri(cancion.getSpotifyUri());
            }
        });
        recyclerView.setAdapter(adapter);
        recyclerView.setAdapter(adapter);

        // Cargar canciones recientes si hay token Spotify
        if (MainActivity.spotifyAccessToken != null) {
            cargarCancionesRecientes();
        } else {
            Log.w(TAG, "Token Spotify no disponible, no se puede cargar canciones.");
            tvSpotifyStatus.setText("❌ Token Spotify no disponible.");
        }

        return view;
    }

    // ======================================
    // Cargar canciones recientes desde Spotify Web API
    // ======================================
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
                                String titulo = item.getTrack().getName();
                                String artista = item.getTrack().getArtists().get(0).getName();
                                String spotifyUri = item.getTrack().getUri();

                                // Obtener la primera imagen del álbum
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

                            // Inicializar o actualizar PlaylistManager
                            if (MainActivity.playlistManager == null) {
                                MainActivity.playlistManager = new PlaylistManager(
                                        MainActivity.globalPlaylist,
                                        MainActivity.getSpotifyAppRemote()
                                );
                            } else {
                                MainActivity.playlistManager.setPlaylist(MainActivity.globalPlaylist);
                            }

                            adapter.notifyDataSetChanged();
                            Log.d(TAG, "✅ Canciones recientes cargadas: " + cancionesRecientes.size());
                            tvSpotifyStatus.setText("✅ Spotify: Datos cargados.");
                        } else {
                            Log.e(TAG, "❌ Error en respuesta: " + response.code());
                            tvSpotifyStatus.setText("❌ Spotify: Error API (" + response.code() + ").");
                        }
                    }

                    @Override
                    public void onFailure(Call<SpotifyRecentlyPlayedResponse> call, Throwable t) {
                        Log.e(TAG, "❌ Fallo al cargar canciones recientes: " + t.getMessage(), t);
                        tvSpotifyStatus.setText("❌ Error al cargar canciones.");
                    }
                });
    }
}
