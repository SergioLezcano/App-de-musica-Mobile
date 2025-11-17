package com.example.appmusic_basico;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import models.Artistas;

public class FragmentProfile extends Fragment{

    private Button btnLogout;
    // Opcional: Si quieres actualizar los detalles de usuario, añade los TextViews aquí:
    // private TextView tvUserName;
    // private ImageView ivProfilePicture;

    // Constructor requerido
    public FragmentProfile() {
        // Constructor público vacío requerido
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Infla el layout profile_fragment.xml
        return inflater.inflate(R.layout.profile_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Enlazar el botón de cerrar sesión del layout
        btnLogout = view.findViewById(R.id.btn_logout);

        // Opcional: Enlazar otros elementos de la UI si se necesitan datos dinámicos
        // tvUserName = view.findViewById(R.id.tv_user_name);
        // ivProfilePicture = view.findViewById(R.id.iv_profile_picture);
        // loadUserProfileData(); // Llama a un método para cargar datos si los tienes

        // 2. Implementar el Listener para Cerrar Sesión
        btnLogout.setOnClickListener(v -> {
            // Verificar si la actividad contenedora es MainActivity
            if (getActivity() instanceof MainActivity) {
                // Limpiar los datos persistentes del usuario (Artistas favoritos, etc.)

                //clearUserDataLocally();

                // Llamar al método de cierre de sesión implementado en MainActivity
                ((MainActivity) getActivity()).logoutSpotify();

                Toast.makeText(getContext(), "Sesión cerrada. Reiniciando autenticación.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Error: La aplicación principal no está disponible.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Limpia los datos locales del usuario que no se manejan con el token (Ej: Artistas Favoritos).
     */
    private void clearUserDataLocally() {
        if (getContext() == null) return;

        SharedPreferences prefs =
                getContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = prefs.edit();

        // 💡 Limpiamos la lista de artistas favoritos
        editor.remove("favorite_artists_json");

        // 💡 Puedes agregar aquí más datos a limpiar (ej: configuración, caché de imágenes)

        editor.apply(); // Aplica los cambios de forma asíncrona
    }
}

