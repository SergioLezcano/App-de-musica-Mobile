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
import adapters.RecentlyPlayedAdapter;
import models.AlbumFavorito; // Importar el nuevo modelo
import models.Cancion_Reciente;

public class FragmentFavourite extends Fragment {

    private static final String TAG = "FragmentFavourite";
    private static final int LAYOUT_RES_ID = R.layout.favourite_activity;
    private RecyclerView rvFavoriteAlbum;
    private AlbumAdapter albumAdapter;
    private List<AlbumFavorito> favoriteAlbums = new ArrayList<>();
    private final Gson gson = new Gson();
    private BroadcastReceiver albumUpdateReceiver;
    private RecyclerView rvFavoriteMusic;
    private RecentlyPlayedAdapter songFavoriteAdapter;
    private List<Cancion_Reciente> favoriteSongs = new ArrayList<>();
    private BroadcastReceiver songUpdateReceiver;

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
        // 🎶 Canciones Favoritas
        // --------------------------------------------
        rvFavoriteMusic = view.findViewById(R.id.rv_favorite_music);
        setupFavoriteSongsRecyclerView(); // Llamada al setup

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

        // --------------------------------------------
        // 📡 Broadcast interno para actualizar Canciones
        // --------------------------------------------
        songUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("SONG_FAVORITE_UPDATE".equals(intent.getAction())) {
                    Log.d(TAG, "🎵 Broadcast recibido → actualizando lista de canciones favoritas...");
                    setupFavoriteSongsRecyclerView();
                }
            }
        };

        IntentFilter songFilter = new IntentFilter("SONG_FAVORITE_UPDATE");
        ContextCompat.registerReceiver(
                requireContext(),
                songUpdateReceiver,
                songFilter,
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

        // Unregister del nuevo receiver
        if (songUpdateReceiver != null) {
            requireContext().unregisterReceiver(songUpdateReceiver);
            songUpdateReceiver = null;
        }
    }

    // ===========================================================
    // GESTIÓN DE ÁLBUMES FAVORITOS
    // ===========================================================

    private void setupFavoriteAlbumsRecyclerView() {
        // 1. Cargar datos de SharedPreferences
        loadFavoriteAlbums();

        // 2. Configurar el RecyclerView
        // Utiliza un LinearLayoutManager horizontal
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

    // ===========================================================
    //NUEVO MÉTODO: GESTIÓN DE CANCIONES FAVORITAS
    // ===========================================================

    private void setupFavoriteSongsRecyclerView() {
        // 1. Cargar datos de SharedPreferences
        loadFavoriteSongs();

        // 2. Configurar el RecyclerView (horizontal, usando el mismo adaptador que Canciones Recientes)
        rvFavoriteMusic.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        // 3. Inicializar y conectar el adaptador
        if (songFavoriteAdapter == null) {
            // Reutilizamos RecentlyPlayedAdapter para mostrar las canciones
            songFavoriteAdapter = new RecentlyPlayedAdapter(favoriteSongs, this::handleSongClick);
            rvFavoriteMusic.setAdapter(songFavoriteAdapter);
        } else {
            songFavoriteAdapter.notifyDataSetChanged();
        }
    }

    private void loadFavoriteSongs() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);

        String json = prefs.getString("favorite_songs_json", "[]"); // 🆕 Nueva clave
        Type type = new TypeToken<List<Cancion_Reciente>>() {}.getType();
        List<Cancion_Reciente> loadedSongs = gson.fromJson(json, type);

        favoriteSongs.clear();
        if (loadedSongs != null) {
            favoriteSongs.addAll(loadedSongs);
        }
    }

    private void handleSongClick(Cancion_Reciente song) {
        // Lógica al hacer clic en una canción favorita para reproducirla
        if (MainActivity.playlistManager != null) {
            MainActivity.playlistManager.playUri(song.getSpotifyUri());
        } else {
            Toast.makeText(getContext(), "Spotify no conectado.", Toast.LENGTH_SHORT).show();
        }
    }
}
