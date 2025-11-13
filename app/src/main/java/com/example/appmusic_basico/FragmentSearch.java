package com.example.appmusic_basico;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appmusic_basico.api.RetrofitClient;
import com.example.appmusic_basico.api.SpotifyArtistSearchResponse;
import com.example.appmusic_basico.api.SpotifySearchGeneralResponse;
import com.example.appmusic_basico.api.SpotifyService;

import java.util.ArrayList;
import java.util.List;

import adapters.SearchResultAdapter; // 🆕 Debes crear este adaptador
import models.SearchResultItem;     // 🆕 Debes crear este modelo unificado
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentSearch extends Fragment implements SearchView.OnQueryTextListener {

    private static final String TAG = "FragmentSearch";

    private SearchView searchView;
    private RecyclerView rvSearchResults;
    private ScrollView scrollViewExploration;
    private SearchResultAdapter searchAdapter;
    private final List<SearchResultItem> resultsList = new ArrayList<>();

    public FragmentSearch() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.search_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        searchView = view.findViewById(R.id.search_view);
        rvSearchResults = view.findViewById(R.id.rv_search_results);
        scrollViewExploration = view.findViewById(R.id.scroll_view_exploration);

        // 1. Configurar SearchView
        searchView.setOnQueryTextListener(this);

        // 2. Configurar RecyclerView de resultados
        searchAdapter = new SearchResultAdapter(resultsList, this::handleSearchResultClick);
        rvSearchResults.setAdapter(searchAdapter);

        // 3. Opcional: Expandir el SearchView por defecto
        searchView.setIconified(false);
    }

    // =========================================================
    // 💡 SearchView Listeners
    // =========================================================

    @Override
    public boolean onQueryTextSubmit(String query) {
        // Disparar la búsqueda cuando el usuario presiona Enter
        if (query != null && !query.trim().isEmpty()) {
            performSpotifySearch(query.trim());
        }
        searchView.clearFocus(); // Ocultar el teclado
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        // Opcional: Disparar la búsqueda en tiempo real (puede ser caro en llamadas API)
        // Por ahora, solo se ejecuta en onQueryTextSubmit.
        if (newText.isEmpty()) {
            // Mostrar exploración si el texto está vacío
            rvSearchResults.setVisibility(View.GONE);
            scrollViewExploration.setVisibility(View.VISIBLE);
            resultsList.clear();
            searchAdapter.notifyDataSetChanged();
        }
        return false;
    }

    // =========================================================
    // 🌐 Búsqueda en Spotify
    // =========================================================

    private void performSpotifySearch(String query) {
        if (MainActivity.spotifyAccessToken == null) {
            Toast.makeText(getContext(), "❌ Spotify no conectado.", Toast.LENGTH_SHORT).show();
            return;
        }

        SpotifyService api = RetrofitClient.getClient().create(SpotifyService.class);

        api.searchAll(
                "Bearer " + MainActivity.spotifyAccessToken,
                query,
                "track,artist,album" // 🛑 CRÍTICO: Buscar los tres tipos
        ).enqueue(new Callback<SpotifySearchGeneralResponse>() {
            @Override
            public void onResponse(Call<SpotifySearchGeneralResponse> call, Response<SpotifySearchGeneralResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    processSearchResults(response.body());
                } else {
                    Log.e(TAG, "❌ Error API Spotify Search: " + response.code());
                    Toast.makeText(getContext(), "Error en la búsqueda.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SpotifySearchGeneralResponse> call, Throwable t) {
                Log.e(TAG, "❌ Fallo de red en búsqueda: " + t.getMessage());
                Toast.makeText(getContext(), "Error de conexión.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // =========================================================
    // 🧩 Procesamiento y Visualización
    // =========================================================

    private void processSearchResults(SpotifySearchGeneralResponse response) {
        resultsList.clear();

        // 1. Procesar Artistas
        if (response.getArtists() != null && response.getArtists().getItems() != null) {
            for (SpotifyArtistSearchResponse.Item item : response.getArtists().getItems()) {
                // Mapear los resultados a tu modelo unificado (SearchResultItem)
                String imageUrl = item.getImages() != null && !item.getImages().isEmpty() ? item.getImages().get(0).getUrl() : null;
                resultsList.add(new SearchResultItem(item.getName(), "Artista", imageUrl, item.getId(), null, "artist"));
            }
        }

        // 2. Procesar Álbumes (Similar a Artistas, requiere el modelo de Álbum)

        // 3. Procesar Canciones (Similar a Artistas, requiere el modelo de Pistas)

        // 4. Mostrar Resultados
        searchAdapter.notifyDataSetChanged();
        scrollViewExploration.setVisibility(View.GONE);
        rvSearchResults.setVisibility(View.VISIBLE);

        if (resultsList.isEmpty()) {
            Toast.makeText(getContext(), "No se encontraron resultados.", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleSearchResultClick(SearchResultItem item) {
        // Implementar la acción al hacer clic en un resultado:
        // - Si es artista: Abrir SecondaryActivity (Top Tracks)
        // - Si es álbum: Abrir AlbumDetailActivity
        // - Si es track: Reproducir la canción

        Toast.makeText(getContext(), "Clic en: " + item.getTitle() + " (" + item.getType() + ")", Toast.LENGTH_SHORT).show();
        // Lógica de reproducción o navegación aquí...
    }
}