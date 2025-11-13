package com.example.appmusic_basico;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import adapters.AlbumAdapter; // Importar el nuevo adaptador
import models.AlbumFavorito; // Importar el nuevo modelo

public class FragmentFavourite extends Fragment {

    private static final String TAG = "FragmentFavourite";
    private static final int LAYOUT_RES_ID = R.layout.favourite_activity;

    private RecyclerView rvFavoriteAlbum;
    private AlbumAdapter albumAdapter;
    private List<AlbumFavorito> favoriteAlbums = new ArrayList<>();

    private final Gson gson = new Gson();
    private BroadcastReceiver albumUpdateReceiver; // Nuevo receiver

    public FragmentFavourite() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(LAYOUT_RES_ID, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --------------------------------------------
        // 💿 Álbumes Favoritos
        // --------------------------------------------
        rvFavoriteAlbum = view.findViewById(R.id.rv_favorite_album);
        setupFavoriteAlbumsRecyclerView();

        // --------------------------------------------
        // 📡 Broadcast interno para actualizar álbumes
        // --------------------------------------------
        albumUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("ALBUM_FAVORITE_UPDATE".equals(intent.getAction())) {
                    Log.d(TAG, "🎵 Broadcast recibido → actualizando lista de álbumes favoritos...");
                    setupFavoriteAlbumsRecyclerView();
                }
            }
        };

        IntentFilter filter = new IntentFilter("ALBUM_FAVORITE_UPDATE");
        // Registrar el receiver
        ContextCompat.registerReceiver(
                requireContext(),
                albumUpdateReceiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (albumUpdateReceiver != null) {
            requireContext().unregisterReceiver(albumUpdateReceiver);
            albumUpdateReceiver = null;
        }
    }

    // ===========================================================
    // 💾 GESTIÓN DE ÁLBUMES FAVORITOS
    // ===========================================================

    private void setupFavoriteAlbumsRecyclerView() {
        // 1. Cargar datos de SharedPreferences
        loadFavoriteAlbums();

        // 2. Configurar el RecyclerView
        // Utiliza un LinearLayoutManager horizontal como en tu XML
        rvFavoriteAlbum.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        // 3. Inicializar y conectar el adaptador
        if (albumAdapter == null) {
            albumAdapter = new AlbumAdapter(favoriteAlbums, this::handleAlbumClick);
            rvFavoriteAlbum.setAdapter(albumAdapter);
        } else {
            // Si ya existe, solo notificar. Aunque loadFavoriteAlbums() también debería hacer esto.
            albumAdapter.notifyDataSetChanged();
        }
    }

    private void loadFavoriteAlbums() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);

        // Usar la clave guardada en ThirdActivity
        String json = prefs.getString("favorite_albums_json", "[]");

        Type type = new TypeToken<List<AlbumFavorito>>() {}.getType();
        List<AlbumFavorito> loadedAlbums = gson.fromJson(json, type);

        favoriteAlbums.clear();
        if (loadedAlbums != null) {
            favoriteAlbums.addAll(loadedAlbums);
        }

        // Si el adaptador ya está inicializado, notificar cambio de datos
        if (albumAdapter != null) {
            albumAdapter.notifyDataSetChanged();
        }
    }

    private void handleAlbumClick(AlbumFavorito album) {
        // Lógica a ejecutar al hacer clic en un álbum favorito
        if (MainActivity.playlistManager != null) {
            // Intentar reproducir el álbum completo usando su URI
            MainActivity.playlistManager.playUri(album.getSpotifyUri());
        } else {
            Toast.makeText(getContext(), "Conexión Spotify no disponible. Intente reconectar.", Toast.LENGTH_SHORT).show();
        }
    }

    // NOTA: También deberías añadir la lógica para rv_favorite_music (Canciones Favoritas)
    // si planeas implementarla con otro modelo de datos y adaptador (ej: FavoriteSongAdapter).
}
