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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appmusic_basico.api.RetrofitClient;
import com.example.appmusic_basico.api.SpotifyArtistSearchResponse;
import com.example.appmusic_basico.api.SpotifySearchGeneralResponse;
import com.example.appmusic_basico.api.SpotifyService;

import java.util.ArrayList;
import java.util.List;

import adapters.CategoryAdapter;
import adapters.SearchResultAdapter;
import models.SearchResultItem;
import adapters.AlbumExplorerAdapter;
import models.AlbumExplorerItem;
import models.CategoryItem;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentSearch extends Fragment implements
        SearchView.OnQueryTextListener,
        CategoryAdapter.OnCategoryClickListener,
        AlbumExplorerAdapter.OnAlbumClickListener {

    private static final String TAG = "FragmentSearch";

    private SearchView searchView;
    private RecyclerView rvSearchResults;
    private ScrollView scrollViewExploration;
    private SearchResultAdapter searchAdapter;
    private final List<SearchResultItem> resultsList = new ArrayList<>();
    private RecyclerView rvCategories;
    private CategoryAdapter categoryAdapter;
    private final List<CategoryItem> categoryList = new ArrayList<>();
    private RecyclerView rvAlbums;
    private AlbumExplorerAdapter albumAdapter;
    private final List<AlbumExplorerItem> albumList = new ArrayList<>();

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

        rvCategories = view.findViewById(R.id.rv_categories);
        // 🆕 Inicializar RecyclerView de Álbumes
        rvAlbums = view.findViewById(R.id.rv_albums);

        // 1. Configurar el Manager (Horizontal para tarjetas)
        rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
//        rvAlbums.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        // 1. Configurar SearchView
        searchView.setOnQueryTextListener(this);

        // 2. Configurar RecyclerView de resultados
        searchAdapter = new SearchResultAdapter(resultsList, this::handleSearchResultClick);
        rvSearchResults.setAdapter(searchAdapter);

        // 3. Opcional: Expandir el SearchView por defecto
        searchView.setIconified(false);

        setupCategoryRecyclerView();
        setupAlbumRecyclerView();
        loadCategories();
        loadNewAlbums();
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

    // =========================================================
    // 🆕 Configuración y Carga de Categorías
    // =========================================================

    private void setupCategoryRecyclerView() {
        // Horizontal Layout para tarjetas
        rvCategories.setLayoutManager(new LinearLayoutManager(
                getContext(),
                LinearLayoutManager.HORIZONTAL,
                false
        ));

        // Asignar el nuevo adaptador
        categoryAdapter = new CategoryAdapter(categoryList, this); // 'this' implementa OnCategoryClickListener
        rvCategories.setAdapter(categoryAdapter);
    }

    private void loadCategories() {
        // 🛑 Nota: Aquí iría la llamada a la API de Spotify: /browse/categories
        // Pero para probar la UI, agregaremos datos de prueba.

        categoryList.add(new CategoryItem("pop", "Pop", "https://i.scdn.co/image/ab67706f00000003b688045e7f1c1f5165d79905"));
        categoryList.add(new CategoryItem("hiphop", "Hip-Hop", "https://i.scdn.co/image/ab67706f00000003666d98e72c01994e634151a7"));
        categoryList.add(new CategoryItem("rock", "Rock", "https://i.scdn.co/image/ab67706f000000037a44a7f0e65389d54e58b14a"));
        categoryList.add(new CategoryItem("workout", "Workout", "https://i.scdn.co/image/ab67706f00000003923c65c490a6042469446d61"));

        categoryAdapter.notifyDataSetChanged();
    }

    // =========================================================
    // 🆕 Implementación del Click de Categoría
    // =========================================================

    @Override
    public void onCategoryClick(CategoryItem category) {
        // Lógica al hacer clic en una tarjeta de categoría
        // 🛑 Siguiente paso: Abrir una lista de reproducción basada en category.getId()
        Toast.makeText(getContext(), "Abriendo categoría: " + category.getName(), Toast.LENGTH_SHORT).show();
    }

    // =========================================================
    // 🆕 Configuración y Carga de Álbumes
    // =========================================================

    private void setupAlbumRecyclerView() {
        rvAlbums.setLayoutManager(new LinearLayoutManager(
                getContext(),
                LinearLayoutManager.HORIZONTAL,
                false
        ));

        albumAdapter = new AlbumExplorerAdapter(albumList, this); // 'this' implementa OnAlbumClickListener
        rvAlbums.setAdapter(albumAdapter);
    }

    private void loadNewAlbums() {
        // 🛑 Nota: Aquí iría la llamada a la API de Spotify: /browse/new-releases
        // Datos de prueba para la UI
        albumList.add(new AlbumExplorerItem("a1", "Certified Lover Boy", "Drake", "https://i.scdn.co/image/ab67616d00001e02404b901a91d1e4e73e2d67a1"));
        albumList.add(new AlbumExplorerItem("a2", "SOUR", "Olivia Rodrigo", "https://i.scdn.co/image/ab67616d00001e02613b4c194b12484a0d9b4b0e"));
        albumList.add(new AlbumExplorerItem("a3", "Midnights", "Taylor Swift", "https://i.scdn.co/image/ab67616d00001e02f92f6943b17c3857db636e78"));

        albumAdapter.notifyDataSetChanged();
    }

    // =========================================================
    // 🆕 Implementación del Click de Álbum
    // =========================================================

    @Override
    public void onAlbumClick(AlbumExplorerItem album) {
        // Lógica al hacer clic en una tarjeta de álbum
        // 🛑 Siguiente paso: Abrir la actividad de detalle de álbum (AlbumDetailActivity)
        Toast.makeText(getContext(), "Abriendo álbum: " + album.getAlbumName(), Toast.LENGTH_SHORT).show();
    }
}