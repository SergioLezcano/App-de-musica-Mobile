package com.example.appmusic_basico;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;


public class FragmentHome extends Fragment{

    private static final int LAYOUT_RES_ID = R.layout.home_fragment;

    public FragmentHome(){

    }

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
    }

}


