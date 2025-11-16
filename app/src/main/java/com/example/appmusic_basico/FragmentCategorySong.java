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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appmusic_basico.api.RetrofitClient;
import com.example.appmusic_basico.api.SpotifyService;
import com.example.appmusic_basico.api.CategoryPlaylistResponse;
import java.util.ArrayList;
import java.util.List;
import adapters.PlaylistGridAdapter;
import models.PlaylistItem;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentCategorySong extends Fragment implements PlaylistGridAdapter.OnSongClickListener {

    private static final String TAG = "FragCategorySong";

    private RecyclerView rvCategoryPlaylists;
    private TextView tvCategoryTitle;
    private PlaylistGridAdapter playlistAdapter;
    private final List<PlaylistItem> playlistList = new ArrayList<>();
    private String categoryId;
    private String categoryName;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflar el diseño del fragmento
        return inflater.inflate(R.layout.fragment_category_song, container, false);
    }

    @Override
    public void onViewCreated (@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Inicializar vistas
        rvCategoryPlaylists = view.findViewById(R.id.rv_lista_categorias);
        tvCategoryTitle = view.findViewById(R.id.tv_titulo_categoria);

        // 2. Obtener argumentos (ID y, si es posible, NOMBRE de Categoría)
        if (getArguments() != null) {
            categoryId = getArguments().getString("CATEGORY_ID", null);
            // 💡 Puedes pasar el NOMBRE de la categoría también desde FragmentSearch para el título
            categoryName = getArguments().getString("CATEGORY_NAME", categoryId); // Usar ID como fallback
        }

        // Si no hay ID, no podemos continuar
        if (categoryId == null) {
            Toast.makeText(getContext(), "Error: ID de categoría no proporcionado.", Toast.LENGTH_LONG).show();
            // Cerrar el fragmento o volver atrás
            return;
        }

        tvCategoryTitle.setText(categoryName.toUpperCase());

        // 3. Configurar el Adaptador y Listener
        // Pasamos la lista y 'this' como el listener
        playlistAdapter = new PlaylistGridAdapter(playlistList, this);

        // 4. Configurar el LayoutManager de Cuadrícula (3 columnas)
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);

        rvCategoryPlaylists.setLayoutManager(layoutManager);
        rvCategoryPlaylists.setAdapter(playlistAdapter);

        // 5. Cargar datos reales: Playlists de la categoría seleccionada
        loadPlaylistsForCategory(categoryId);
    }

    // -------------------------------------------------------------------
    // 🌐 Lógica de Carga de Playlists de Categoría (API Real)
    // -------------------------------------------------------------------

    private void loadPlaylistsForCategory(String id) {
        if (MainActivity.spotifyAccessToken == null) {
            Toast.makeText(getContext(), "❌ Spotify no conectado.", Toast.LENGTH_SHORT).show();
            Toast.makeText(getContext(), "❌ Spotify no conectado. Intente de nuevo.", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "🔎 ID de categoría a buscar: " + id + ", Mercado: US");

        SpotifyService api = RetrofitClient.getClient().create(SpotifyService.class);

        api.getCategoryPlaylists(
                "Bearer " + MainActivity.spotifyAccessToken,
                id,
                "US",
                50    // Límite de ítems
        ).enqueue(new Callback<CategoryPlaylistResponse>() {
            @Override
            public void onResponse(Call<CategoryPlaylistResponse> call, Response<CategoryPlaylistResponse> response) {

                if (response.isSuccessful() && response.body() != null && response.body().getPlaylists() != null) {
                    // 🟢 Llama a la nueva función de procesamiento que verifica .isEmpty()
                    processPlaylists(response.body().getPlaylists().getItems());

                } else if (response.code() == 404) {
                    // ❌ Código 404: La categoría NO tiene contenido activo o no existe en este mercado.
                    Log.e(TAG, "❌ Error 404: Categoría inactiva para el mercado");
                    Toast.makeText(getContext(), "No se encontraron playlists para esta categoría en tu región.", Toast.LENGTH_LONG).show();

                } else {
                    // ❌ Otros errores (400, 500, etc.)
                    Log.e(TAG, "❌ Error al cargar playlists: " + response.code());
                    Toast.makeText(getContext(), "Error al cargar playlists.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CategoryPlaylistResponse> call, Throwable t) {
                Log.e(TAG, "❌ Fallo de red en playlists: " + t.getMessage());
                Toast.makeText(getContext(), "Error de conexión.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void processPlaylists(List<CategoryPlaylistResponse.Playlists.Item> playlistItems) {
        playlistList.clear();

        // 🎯 Paso 1: Verificar si la lista de ítems está vacía
        if (playlistItems == null || playlistItems.isEmpty()) {

            // ❌ ¡No hay playlists para esta categoría en el mercado actual!
            Toast.makeText(getContext(), "Esta categoría no tiene playlists disponibles en el mercado seleccionado.", Toast.LENGTH_LONG).show();

            // Opcional: Navegar hacia atrás o mostrar un mensaje de vacío
            // if (getActivity() != null) {
            //     getActivity().getSupportFragmentManager().popBackStack();
            // }

            playlistAdapter.notifyDataSetChanged();
            return;
        }

        // 🟢 Si llegamos aquí, sí hay playlists.

        for (CategoryPlaylistResponse.Playlists.Item item : playlistItems) {
            // ... (Tu lógica existente para mapear Item a SongItem)
            String imageUrl = null;
            if (item.getImages() != null && !item.getImages().isEmpty()) {
                imageUrl = item.getImages().get(0).getUrl();
            }

            String subtitle = (item.getOwner() != null && item.getOwner().getDisplayName() != null)
                    ? item.getOwner().getDisplayName()
                    : "Playlist de Spotify";;

            playlistList.add(new PlaylistItem(
                    item.getId(),
                    item.getName(),
                    subtitle,
                    imageUrl
            ));
        }

        playlistAdapter.notifyDataSetChanged();
        Log.d(TAG, "Playlists cargadas: " + playlistList.size());
    }


    // -------------------------------------------------------------------
    // 🎧 Implementación de la Interfaz de Click de Canción/Playlist
    // -------------------------------------------------------------------

    @Override
    public void onSongClick(PlaylistItem song) {
        MainActivity activity = (MainActivity) getActivity();

        if (activity != null) {

            // 💡 Para reproducir una playlist, necesitamos la URI de Spotify.
            // La URI se almacena en el modelo de respuesta Item.
            // Si SongItem tiene un campo para la URI (ideal), úsalo.
            // Si solo tienes el ID, debes construir la URI: spotify:playlist:<ID>

            String playlistUri = "spotify:playlist:" + song.getId();

            activity.playSpotifyUri(playlistUri);

            Toast.makeText(getContext(), "Reproduciendo Playlist: " + song.getTitle(), Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(getContext(), "Error al iniciar reproducción.", Toast.LENGTH_SHORT).show();
        }
    }
}