package com.example.appmusic_basico;


import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import adapters.ArtistAdapter;
import adapters.RecentlyPlayedAdapter;
import models.Cancion_Reciente;
import models.Artistas;


public class FragmentHome extends Fragment implements ArtistAdapter.OnItemClickListener, RecentlyPlayedAdapter.OnItemClickListener{

    private static final int LAYOUT_RES_ID = R.layout.home_fragment;

    public FragmentHome(){

    }

    //Metodo para inflar (mostar) el home_fragment
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        return inflater.inflate(LAYOUT_RES_ID, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 1. Identificar el elemento que se puede hacer clic (el contenedor de "Nuevo Lanzamiento")
        ImageView nuevoLanzamiento = view.findViewById(R.id.iv_new_release_album_art);
        FrameLayout play_button = view.findViewById(R.id.fl_play_button);

        if (nuevoLanzamiento != null){
            // 2. Establecer el Listener de click
            nuevoLanzamiento.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 3. Crear el Intent para iniciar SecondaryActivity
                    Intent intent = new Intent(getActivity(), SecondaryActivity.class);

                    // 4. Iniciar la Activity
                    startActivity(intent);
                }
            });
        }
        // Lógica para que el boton play del la sección nuevos lanzamientos pase y muestre al ThirdActivity
        if (play_button != null){

            play_button.setOnClickListener(new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), ThirdActivity.class);
                        startActivity(intent);
                  }
            });
        }

        // 2. Lógica para la sección "Tus Artistas Favoritos"
        // Referencia del recyclerView de Artistas favoritos
        RecyclerView rvArtistasFavoritos = view.findViewById(R.id.rv_favorite_artists);

        // b. Preparar el DataSet (Lista de datos)
        List<Artistas> artistasFavoritos = getMockArtistas();

        // c. Configurar el LayoutManager
        rvArtistasFavoritos.setLayoutManager(new LinearLayoutManager(
                getContext(),
                LinearLayoutManager.HORIZONTAL,
                false
        ));

        // d. Crear y asignar el Adapter (CREAR LA CLASE: ArtistAdapter)
        ArtistAdapter adapterArtistas = new ArtistAdapter(artistasFavoritos, this);
        rvArtistasFavoritos.setAdapter(adapterArtistas);


        // 1.Logica para la seccion de Reproducido recientemente
        RecyclerView rvRecentlyPlayed = view.findViewById(R.id.rv_recently_played);

        // 2. Prepara los datos (Datos Mock/Simulados)
        List<Cancion_Reciente> cancionesRecientes = getMockCanciones();

        // 3. Configura el LayoutManager (Horizontal)
        rvRecentlyPlayed.setLayoutManager(new LinearLayoutManager(
                getContext(),
                LinearLayoutManager.HORIZONTAL,
               false
        ));
        // 4. Crear y asignar el Adapter (CREA LA CLASE: Cancion_Reciente)
        RecentlyPlayedAdapter adapterCanciones = new RecentlyPlayedAdapter(cancionesRecientes, this);
        rvRecentlyPlayed.setAdapter(adapterCanciones);
    }

    // --- Implementación de la Interface de Clicks (ArtistAdapter.OnItemClickListener) ---
    @Override
    public void onItemClick(Artistas artista) {
        // Esta función se ejecuta cuando el usuario toca un ítem de la lista de artistas.

        Toast.makeText(getContext(), "Has seleccionado a: " + artista.getArtista(), Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onItemClick(Cancion_Reciente cancionReciente) {
        Toast.makeText(getContext(), "Reproduciendo: " + cancionReciente.getTitulo() + " de " + cancionReciente.getArtistaName(), Toast.LENGTH_SHORT).show();
        // Aquí deberia iniciar una Activity o un PlayerService
    }

    private List<Artistas> getMockArtistas() {
        List<Artistas> artistasFicticios = new ArrayList<>();
        artistasFicticios.add(new Artistas(
                "1er artista de prueba",
                R.drawable.image_1034
        ));
        artistasFicticios.add(new Artistas(
                "2do artista de prueba",
                R.drawable.image_2930
        ));
        artistasFicticios.add(new Artistas(
                "3er artista de prueba",
                R.drawable.image_2930
        ));
        return artistasFicticios;
    }

    private List<Cancion_Reciente> getMockCanciones() {
        // Simulación de carga de datos para "Reproducido Recientemente"
        List<Cancion_Reciente> canciones = new ArrayList<>();
        canciones.add(new Cancion_Reciente("Canción 1", "Artista X", R.drawable.image_2930));
        canciones.add(new Cancion_Reciente("Canción 2", "Artista Y", R.drawable.image_2930));
        canciones.add(new Cancion_Reciente("Canción 3", "Artista W", R.drawable.image_2930));
        return canciones;
    }

    private MediaPlayer mediaPlayer;

    // Método de la interfaz de clic que se llama desde el Adapter
    public void onSongClick(Cancion_Reciente cancion) {
        // 1. Detener cualquier reproducción anterior
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        // 2. Implementar la inicialización y reproducción
        try {
            // Obtenemos el ID de recurso (ej: R.raw.song_a)
            int recurso = cancion.getResourceId();

            // Inicializar el MediaPlayer con el recurso local
            mediaPlayer = MediaPlayer.create(getContext(), recurso);

            // Iniciar la reproducción
            mediaPlayer.start();

            Toast.makeText(getContext(), "Reproduciendo: " + cancion.getTitulo(), Toast.LENGTH_SHORT).show();
            Toast.makeText(getContext(), "Artista" + cancion.getArtistaName(), Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(getContext(), "Error al iniciar la reproducción.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    //⚠️ Importante: Liberar el MediaPlayer cuando el Fragment sea destruido
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release(); // Libera los recursos del sistema
            mediaPlayer = null;
        }
    }

}


