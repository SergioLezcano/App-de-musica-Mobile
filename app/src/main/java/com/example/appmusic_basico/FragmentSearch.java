package com.example.appmusic_basico;

import android.content.Intent;
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
import com.example.appmusic_basico.api.SpotifyAlbumSearchResponse;
import com.example.appmusic_basico.api.SpotifyArtistSearchResponse;
import com.example.appmusic_basico.api.SpotifyCategoriesResponse;
import com.example.appmusic_basico.api.SpotifySearchGeneralResponse;
import com.example.appmusic_basico.api.SpotifyService;
import com.example.appmusic_basico.api.SpotifyTrackSearchResponse;
import com.example.appmusic_basico.SecondaryActivity;

import java.util.ArrayList;
import java.util.List;

import adapters.AlbumAdapter;
import adapters.CategoryAdapter;
import adapters.SearchResultAdapter;
import models.AlbumItem;
import models.ArtistItem;
import models.ArtistsResponse;
import models.NewReleasesResponse;
import models.SearchResultItem;
import adapters.AlbumExplorerAdapter;
import models.AlbumExplorerItem;
import models.CategoryItem;
import adapters.ArtistExplorerAdapter;
import models.ArtistExplorerItem;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentSearch extends Fragment implements
        SearchView.OnQueryTextListener,
        CategoryAdapter.OnCategoryClickListener,
        AlbumExplorerAdapter.OnAlbumClickListener,
        ArtistExplorerAdapter.OnArtistClickListener {

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
    private AlbumExplorerAdapter albumExplorerAdapter;
    private final List<AlbumExplorerItem> albumList = new ArrayList<>();
    private RecyclerView rvArtists;
    private ArtistExplorerAdapter artistAdapter;
    private final List<ArtistExplorerItem> artistList = new ArrayList<>();

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
        rvAlbums = view.findViewById(R.id.rv_albums);
        rvArtists = view.findViewById(R.id.rv_artistas);

        // 1. Configurar SearchView
        searchView.setOnQueryTextListener(this);

        // 2. Configurar RecyclerView de resultados
        searchAdapter = new SearchResultAdapter(resultsList, this::handleSearchResultClick);
        rvSearchResults.setAdapter(searchAdapter);

        // 3. Configurar RecyclerViews de Exploración
        setupCategoryRecyclerView();
        setupAlbumRecyclerView();
        setupArtistRecyclerView();

        // 4. Cargar datos de exploración

        loadCategories();
        loadNewAlbums();
        loadPopularArtists();
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
                String imageUrl = item.getImages() != null && !item.getImages().isEmpty() ? item.getImages().get(0).getUrl() : null;

                // Artista: Título=Nombre del Artista, Subtítulo="Artista"
                resultsList.add(new SearchResultItem(
                        item.getName(),     // title
                        "Artista",          // subtitle
                        imageUrl,           // imageUrl
                        item.getId(),       // spotifyId
                        null,               // spotifyUri (No aplica para artistas)
                        "artist"            // type
                ));
            }
        }

        // --- Separador de lógica ---

        // 2. Procesar Álbumes
        if (response.getAlbums() != null && response.getAlbums().getItems() != null) {
            for (SpotifyAlbumSearchResponse.Item item : response.getAlbums().getItems()) {

                String artistName = "";
                if (item.getArtists() != null && !item.getArtists().isEmpty()) {
                    artistName = item.getArtists().get(0).getName();
                }

                String imageUrl = null;
                if (item.getImages() != null && !item.getImages().isEmpty()) {
                    imageUrl = item.getImages().get(0).getUrl();
                }

                // Álbum: Título=Nombre del Álbum, Subtítulo=Nombre del Artista
                resultsList.add(new SearchResultItem(
                        item.getName(),     // title (Nombre del Álbum)
                        artistName,         // subtitle (Nombre del Artista)
                        imageUrl,           // imageUrl
                        item.getId(),       // spotifyId
                        item.getUri(),      // spotifyUri
                        "album"             // type
                ));
            }
        }

        // --- Separador de lógica ---

        // 3. Procesar Canciones (Tracks) 🎶
        if (response.getTracks() != null && response.getTracks().getItems() != null) {
            for (SpotifyTrackSearchResponse.Item item : response.getTracks().getItems()) {

                // Obtener el nombre del artista principal
                String artistName = "";
                if (item.getArtists() != null && !item.getArtists().isEmpty()) {
                    artistName = item.getArtists().get(0).getName();
                }

                // Obtener la URL de la imagen (de la carátula del álbum de la canción)
                String imageUrl = null;
                if (item.getAlbum() != null && item.getAlbum().getImages() != null && !item.getAlbum().getImages().isEmpty()) {
                    imageUrl = item.getAlbum().getImages().get(0).getUrl();
                }

                // Canción: Título=Nombre de la Canción, Subtítulo=Nombre del Artista
                resultsList.add(new SearchResultItem(
                        item.getName(),     // title (Nombre de la Canción)
                        artistName,         // subtitle (Nombre del Artista)
                        imageUrl,           // imageUrl (Carátula del álbum)
                        item.getId(),       // spotifyId
                        item.getUri(),      // spotifyUri 👈 La canción SÍ tiene URI para reproducción
                        "track"             // type
                ));
            }
        }

        // 4. Mostrar Resultados
        searchAdapter.notifyDataSetChanged();

        // Control de visibilidad
        scrollViewExploration.setVisibility(View.GONE);
        rvSearchResults.setVisibility(View.VISIBLE);

        if (resultsList.isEmpty()) {
            Toast.makeText(getContext(), "No se encontraron resultados.", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleSearchResultClick(SearchResultItem item) {

        Toast.makeText(getContext(), "Clic en: " + item.getTitle() + " (" + item.getType() + ")", Toast.LENGTH_SHORT).show();

        // Lógica de reproducción o navegación
        switch (item.getType()) {
            case "artist":
                openArtistTopTracks(item.getSpotifyId());
                break;
            case "album":
                openAlbumDetail(item);
                break;
            case "track":
                playTrack(item);
                break;
        }
    }

    // 🆕 Método para manejar la navegación a Top Tracks
    private void openArtistTopTracks(String artistId) {
        if (getContext() != null) {
            // 1. Crear el Intent para SecondaryActivity
            Intent intent = new Intent(getContext(), SecondaryActivity.class);

            // 2. Adjuntar los datos necesarios (ID del artista)
            // Usamos una clave constante (ej. "ARTIST_ID") para recuperarla en SecondaryActivity
            intent.putExtra("ARTIST_ID", artistId);

            // 3. Iniciar la actividad
            startActivity(intent);
        }
    }

    // 🆕 Método para manejar la reproducción de una canción
    private void playTrack(SearchResultItem trackItem) {
        MainActivity activity = (MainActivity) getActivity();

        if (activity != null && trackItem.getSpotifyUri() != null) {

            // 1. Reproducir la canción usando el método de MainActivity.
            // MainActivity se encargará de usar mSpotifyAppRemote y playlistManager.
            activity.playSpotifyUri(trackItem.getSpotifyUri());

            // 2. Notificar a MainActivity para que registre la canción como reciente
            // y actualice el minirreproductor.
            activity.trackPlayed(trackItem);

            Toast.makeText(getContext(), "Reproduciendo: " + trackItem.getTitle(), Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(getContext(), "No se puede reproducir. Player no disponible o URI faltante.", Toast.LENGTH_SHORT).show();
        }
    }

    // 🆕 Método para manejar la navegación a los detalles del Álbum
    private void openAlbumDetail(SearchResultItem albumItem) {
        if (getContext() != null) {
            Intent intent = new Intent(getContext(), AlbumDetalleActivity.class);

            // Pasamos los datos esenciales del álbum a la nueva Activity
            intent.putExtra("ALBUM_ID", albumItem.getSpotifyId());
            intent.putExtra("ALBUM_URI", albumItem.getSpotifyUri());
            intent.putExtra("ALBUM_NAME", albumItem.getTitle());
            intent.putExtra("ARTIST_NAME", albumItem.getSubtitle());
            intent.putExtra("ALBUM_IMAGE_URL", albumItem.getImageUrl());

            startActivity(intent);
        }
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
        // 🛑 Nota: Si el token no está listo, el método no se ejecuta.
        if (MainActivity.spotifyAccessToken == null) {
            Log.w(TAG, "⚠️ Token no disponible. Categorías no cargadas.");
            return;
        }

        SpotifyService api = RetrofitClient.getClient().create(SpotifyService.class);

        // Usamos "US" como mercado por defecto para asegurar que haya contenido
        api.getAllCategories("Bearer " + MainActivity.spotifyAccessToken, "US", 50)
                .enqueue(new Callback<SpotifyCategoriesResponse>() {
                    @Override
                    public void onResponse(Call<SpotifyCategoriesResponse> call, Response<SpotifyCategoriesResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {

                            // 🟢 ÉXITO: Los datos se reciben
                            categoryList.clear();

                            if (response.body().getCategories() != null && response.body().getCategories().getItems() != null) {
                                for (SpotifyCategoriesResponse.Categories.Item item : response.body().getCategories().getItems()) {
                                    Log.d("CAT-ID", "ID recibido: " + item.getId() + " Name: " + item.getName());
                                    categoryList.add(new CategoryItem(
                                            item.getId(),           // ID de la API
                                            item.getName(),         // Nombre de la API
                                            generateRandomColor()   // Color dinámico
                                    ));
                                }
                                Log.d(TAG, "✅ Categorías cargadas desde la API: " + categoryList.size());
                            }

                        } else {
                            Log.e(TAG, "❌ Fallo HTTP al cargar categorías: " + response.code());
                            // Aquí podrías llamar a un método loadHardcodedCategories() si deseas un fallback.
                        }
                        categoryAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(Call<SpotifyCategoriesResponse> call, Throwable t) {
                        Log.e(TAG, "❌ Fallo de red al cargar categorías: " + t.getMessage());
                    }
                });
    }

    // =========================================================
    // 🆕 Implementación del Click de Categoría
    // =========================================================

    @Override
    public void onCategoryClick(CategoryItem category) {
        Toast.makeText(getContext(), "Abriendo categoría: " + category.getName(), Toast.LENGTH_SHORT).show();
//        Log.d("CAT-CLICK", "Click en categoría: " + category.getId());
//        // 1. Crear el Fragmento de Destino
//        FragmentCategorySong categorySongFragment = new FragmentCategorySong();
//
//        // 2. Preparar los argumentos para pasar el ID de la categoría
//        Bundle args = new Bundle();
//        // Usamos la clave "CATEGORY_ID" que FragmentCategorySong espera
//        args.putString("CATEGORY_ID", category.getId());
//        categorySongFragment.setArguments(args);
//
//        // 3. Realizar la Transacción del Fragmento usando el FragmentManager de la Activity
//        if (getActivity() != null) {
//            getActivity().getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.fragment_container, categorySongFragment)
//                    .addToBackStack(null)
//                    .commit();
//
//            Toast.makeText(getContext(), "Abriendo categoría: " + category.getName(), Toast.LENGTH_SHORT).show();
//        }
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

        albumExplorerAdapter = new AlbumExplorerAdapter(albumList, this); // 'this' implementa OnAlbumClickListener
        rvAlbums.setAdapter(albumExplorerAdapter);
    }

    private void loadNewAlbums() {
        if (MainActivity.spotifyAccessToken == null) return;

        SpotifyService api = RetrofitClient.getClient().create(SpotifyService.class);

        String countryCode = "US";

        api.getNewReleases("Bearer " + MainActivity.spotifyAccessToken, countryCode, 20)
                .enqueue(new Callback<NewReleasesResponse>() {
                    @Override
                    public void onResponse(Call<NewReleasesResponse> call, Response<NewReleasesResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {

                            albumList.clear(); // Limpiar la lista anterior

                            List<AlbumItem> newReleasesFromApi = response.body().getAlbums().getItems();

                            for (AlbumItem apiItem : newReleasesFromApi) {
                                // 💡 Lógica de mapeo: Convertir el modelo de la API al modelo de la UI
                                String artistName = (apiItem.getArtists() != null && !apiItem.getArtists().isEmpty())
                                        ? apiItem.getArtists().get(0).getName()
                                        : "Artista Desconocido";

                                String imageUrl = (apiItem.getImages() != null && !apiItem.getImages().isEmpty())
                                        ? apiItem.getImages().get(0).getUrl()
                                        : null;

                                albumList.add(new AlbumExplorerItem(
                                        apiItem.getId(),           // ID del Álbum
                                        apiItem.getName(),         // Nombre del Álbum
                                        artistName,                // Nombre del Artista
                                        imageUrl,                  // Carátula
                                        generateRandomColor()      // Color de la tarjeta
                                ));
                            }

                            albumExplorerAdapter.notifyDataSetChanged();

                        } else {
                            Log.e(TAG, "Fallo al cargar álbumes: " + response.code());
                            try {
                                Log.e(TAG, "❌ Error HTTP al cargar álbumes: " + response.code() + " - " + response.errorBody().string());
                            } catch (Exception e) {
                                Log.e(TAG, "❌ Error HTTP: " + response.code());
                            }
                            // Manejar errores (ej. mostrar un Toast o un mensaje en la UI)
                        }
                    }

                    @Override
                    public void onFailure(Call<NewReleasesResponse> call, Throwable t) {
                        Log.e(TAG, "❌ Fallo de red al cargar álbumes: " + t.getMessage());
                    }
                });
    }

    public void reloadExplorationData() {
        Log.d(TAG, "Recarga forzada de datos de exploración.");
        loadNewAlbums();
        loadPopularArtists();
        loadCategories();
    }

    private String generateRandomColor() {
        // 1. Inicializa la clase Random
        java.util.Random random = new java.util.Random();

        // 2. Genera un número entero aleatorio.
        // Usamos el bitwise AND (&) con 0xCCCCCC para enmascarar el color.
        // 💡 NOTA: En lugar de usar 0xFFFFFF (que puede generar colores muy blancos/pálidos),
        // usar un valor más bajo como 0xCCCCCC (un gris claro) como máximo
        // ayuda a asegurar que los colores resultantes sean más oscuros y tengan
        // mejor contraste con el texto claro (si lo usas) o sean visualmente más ricos.
        int color = 0xFF000000 | (0xFFFFFF & random.nextInt());

        // Si quieres un enfoque más simple que asegura mejor contraste con texto BLANCO,
        // podrías limitar el valor para obtener colores más oscuros:
        // int color = 0xFF000000 + random.nextInt(0xAA0000);

        // 3. Convierte el entero a su representación hexadecimal.
        // %06X asegura que el número se formatee con 6 dígitos hexadecimales, rellenando con ceros.
        // Usamos color & 0xFFFFFF para ignorar el canal alfa si es necesario
        String hexColor = String.format("#%06X", (color & 0xFFFFFF));

        return hexColor;
    }

    // =========================================================
    // 🆕 Implementación del Click de Álbum
    // =========================================================

    @Override
    public void onAlbumClick(AlbumExplorerItem album) {
        if (getContext() != null) {
            // Asumiendo que AlbumDetalleActivity es la clase correcta para el detalle
            Intent intent = new Intent(getContext(), AlbumDetalleActivity.class);

            // Pasamos los datos esenciales del álbum a la nueva Activity
            intent.putExtra("ALBUM_ID", album.getId());
            intent.putExtra("ALBUM_URI", (String) null);
            intent.putExtra("ALBUM_NAME", album.getAlbumName());
            intent.putExtra("ARTIST_NAME", album.getArtistName());
            intent.putExtra("ALBUM_IMAGE_URL", album.getImageUrl());

            startActivity(intent);
            Toast.makeText(getContext(), "Abriendo álbum: " + album.getAlbumName(), Toast.LENGTH_SHORT).show();
        }
    }

    // =========================================================
    // 🆕 Configuración y Carga de Artistas
    // =========================================================

    private void setupArtistRecyclerView() {
        rvArtists.setLayoutManager(new LinearLayoutManager(
                getContext(),
                LinearLayoutManager.HORIZONTAL,
                false
        ));

        artistAdapter = new ArtistExplorerAdapter(artistList, this); // 'this' implementa OnArtistClickListener
        rvArtists.setAdapter(artistAdapter);
    }

    // FragmentSearch.java

    private void loadPopularArtists() {
        if (MainActivity.spotifyAccessToken == null) return;

        // 1. Definir la lista de IDs que quieres buscar
        List<String> artistIdList = new ArrayList<>();
        artistIdList.add("0h1zs4CTlU9D2QtgPxptUD");
        artistIdList.add("1DxLCyH42yaHKGK3cl5bvG");
        artistIdList.add("4bw2Am3p9ji3mYsXNXtQcd");
        artistIdList.add("0AqlFI0tz2DsEoJlKSIiT9");
        artistIdList.add("3ghRXw2nUEH2THaL82hw8R");
        artistIdList.add("5C4PDR4LnhZTbVnKWXuDKD");

        String popularArtistIds = String.join(",", artistIdList);
        // Si la lista está vacía, salimos
        if (popularArtistIds.isEmpty()) return;

        SpotifyService api = RetrofitClient.getClient().create(SpotifyService.class);

        api.getMultipleArtists("Bearer " + MainActivity.spotifyAccessToken, popularArtistIds)
                .enqueue(new Callback<ArtistsResponse>() {
                    @Override
                    public void onResponse(Call<ArtistsResponse> call, Response<ArtistsResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            artistList.clear();

                            // Asegúrate de que ArtistsResponse tenga getArtists()
                            for (ArtistItem apiItem : response.body().getArtists()) {

                                // 💡 Lógica de mapeo similar a loadNewAlbums()
                                String imageUrl = (apiItem.getImages() != null && !apiItem.getImages().isEmpty())
                                        ? apiItem.getImages().get(0).getUrl()
                                        : null;

                                artistList.add(new ArtistExplorerItem(
                                        apiItem.getId(),           // ID del Artista
                                        apiItem.getName(),         // Nombre del Artista
                                        imageUrl,                  // Imagen
                                        generateRandomColor()      // Color de la tarjeta
                                ));
                            }
                            artistAdapter.notifyDataSetChanged();
                            Log.d(TAG, "✅ Artistas recibidos: " + artistList.size());
                        } else {
                            Log.e(TAG, "❌ Error API al cargar artistas: " + response.code());
                            loadHardcodedArtists(); // ⬅️ Mantener fallback
                        }
                    }
                    @Override
                    public void onFailure(Call<ArtistsResponse> call, Throwable t) {
                        Log.e(TAG, "❌ Fallo de red al cargar artistas: " + t.getMessage());
                        loadHardcodedArtists(); // ⬅️ Mantener fallback
                    }
                });
    }

    // 💡 Debes crear este método para tu lógica hardcodeada/fallback
    private void loadHardcodedArtists() {
        artistList.clear();
        artistList.add(new ArtistExplorerItem("b1", "Bad Bunny", "url_img", generateRandomColor()));
        // ...
        artistAdapter.notifyDataSetChanged();
    }

    // =========================================================
    // 🆕 Implementación del Click de Artista
    // =========================================================

    @Override
    public void onArtistClick(ArtistExplorerItem artist) {
        // Se usa SecondaryActivity para mostrar los Top Tracks, como se define en el código
        if (getContext() != null) {
            Intent intent = new Intent(getContext(), SecondaryActivity.class);

            // Adjuntar los datos necesarios (ID y nombre del artista)

            intent.putExtra("ARTIST_ID", artist.getId());
            intent.putExtra("ARTIST_NAME", artist.getArtistName());

            startActivity(intent);
            Toast.makeText(getContext(), "Abriendo artista: " + artist.getArtistName(), Toast.LENGTH_SHORT).show();
        }
    }

}