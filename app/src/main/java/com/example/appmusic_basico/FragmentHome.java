package com.example.appmusic_basico;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appmusic_basico.api.RetrofitClient;
import com.example.appmusic_basico.api.SpotifyRecentlyPlayedResponse;
import com.example.appmusic_basico.api.SpotifyService;

import java.util.ArrayList;
import java.util.List;

import adapters.RecentlyPlayedAdapter;
import models.Cancion_Reciente;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentHome extends Fragment implements RecentlyPlayedAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private RecentlyPlayedAdapter adapter;
    private List<Cancion_Reciente> cancionesRecientes = new ArrayList<>();
    private TextView tvSpotifyStatus; // Nuevo TextView para mostrar el estado del token
    private static final String TAG = "FragmentHome";

    // =========================================================================
    // CICLO DE VIDA
    // =========================================================================

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.home_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- Mini sección: Nuevo Lanzamiento ---
        ImageView cover = view.findViewById(R.id.iv_new_release_album_art);
        FrameLayout playBtn = view.findViewById(R.id.fl_play_button);
        TextView titleNew = view.findViewById(R.id.tv_track_title);

        // 💡 Inicializar TextView de estado
        tvSpotifyStatus = view.findViewById(R.id.tv_spotify_status);

        titleNew.setText("Never Gonna Give You Up");

        // ❌ Play Button comentado, solo se usa para el ejemplo
        // playBtn.setOnClickListener(v -> {
        //     if (getActivity() instanceof MainActivity) {
        //         ((MainActivity) getActivity()).playSongFromFragment(0);
        //     }
        // });

        cover.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), ThirdActivity.class))
        );

        // --- RecyclerView de canciones recientes ---
        recyclerView = view.findViewById(R.id.rv_recently_played);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        adapter = new RecentlyPlayedAdapter(cancionesRecientes, this);
        recyclerView.setAdapter(adapter);

        // 🔥 Cargar canciones recientes desde la API de Spotify
        cargarCancionesRecientes();
    }

    // 💡 Método para reintentar la carga (útil si la autenticación es asíncrona)
    @Override
    public void onResume() {
        super.onResume();
        // Intentar cargar datos de nuevo si regresamos al fragmento y el token ya existe
        if (MainActivity.spotifyAccessToken != null && cancionesRecientes.isEmpty()) {
            cargarCancionesRecientes();
        }
    }


    // =========================================================================
    // LÓGICA DE LA API DE SPOTIFY
    // =========================================================================

    public void cargarCancionesRecientes() {
        // ⚠️ Acceso al token como variable estática pública de MainActivity
        String accessToken = MainActivity.spotifyAccessToken;

        if (accessToken == null || accessToken.isEmpty()) {
            Log.e(TAG, "❌ No hay token disponible. La MainActivity debe iniciar la autenticación.");
            if (tvSpotifyStatus != null) {
                tvSpotifyStatus.setText("❌ Spotify: Esperando inicio de sesión...");
            }
            // Opcional: Si el token falta, podrías forzar la apertura de la autenticación
            // if (getActivity() instanceof MainActivity) {
            //     ((MainActivity) getActivity()).startSpotifyWebAuth();
            // }
            return;
        }

        if (tvSpotifyStatus != null) {
            tvSpotifyStatus.setText("✅ Spotify: Token OK. Cargando datos...");
        }

        SpotifyService api = RetrofitClient.getClient().create(SpotifyService.class);
        api.getRecentlyPlayed("Bearer " + accessToken).enqueue(new Callback<SpotifyRecentlyPlayedResponse>() {
            @Override
            public void onResponse(Call<SpotifyRecentlyPlayedResponse> call, Response<SpotifyRecentlyPlayedResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cancionesRecientes.clear();

                    for (SpotifyRecentlyPlayedResponse.Item item : response.body().getItems()) {
                        String titulo = item.getTrack().getName();
                        String artista = item.getTrack().getArtists().get(0).getName();

                        // Nota: El recurso de imagen R.drawable.image_1034 debe existir.
                        cancionesRecientes.add(
                                new Cancion_Reciente(titulo, artista, R.drawable.image_1034, 0)
                        );
                    }

                    adapter.notifyDataSetChanged();
                    Log.d(TAG, "✅ Canciones recientes cargadas: " + cancionesRecientes.size());
                    if (tvSpotifyStatus != null) {
                        tvSpotifyStatus.setText("✅ Spotify: Datos cargados.");
                    }

                } else {
                    Log.e(TAG, "❌ Error en respuesta: " + response.code() + " - Mensaje: " + response.errorBody());
                    if (tvSpotifyStatus != null) {
                        tvSpotifyStatus.setText("❌ Spotify: Error API (" + response.code() + ").");
                    }
                    if (response.code() == 401) {
                        // El token ha expirado. Forzar reautenticación en MainActivity
                        Toast.makeText(getContext(), "Token expirado. Reintentando iniciar sesión.", Toast.LENGTH_LONG).show();
                        if (getActivity() instanceof MainActivity) {
                            ((MainActivity) getActivity()).authenticateSpotify();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<SpotifyRecentlyPlayedResponse> call, Throwable t) {
                Log.e(TAG, "❌ Fallo de conexión: " + t.getMessage(), t);
                if (tvSpotifyStatus != null) {
                    tvSpotifyStatus.setText("❌ Spotify: Fallo de red.");
                }
            }
        });
    }

    // =========================================================================
    // MANEJO DE EVENTOS
    // =========================================================================

    @Override
    public void onItemClick(Cancion_Reciente cancion) {
        if (getActivity() instanceof MainActivity) {
            // La función playSongFromFragment debe estar implementada en MainActivity
            ((MainActivity) getActivity()).playSongFromFragment(cancion.getSongResourceId());
        }
    }
}
