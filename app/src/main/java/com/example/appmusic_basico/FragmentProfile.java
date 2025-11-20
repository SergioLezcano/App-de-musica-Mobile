package com.example.appmusic_basico;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.appmusic_basico.api.RetrofitClient;
import com.example.appmusic_basico.api.SpotifyService;
import com.example.appmusic_basico.api.SpotifyUserProfileResponse;

import models.Artistas;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentProfile extends Fragment{

    private Button btnLogout;
    private TextView tvUserName;
    private ImageView ivProfilePicture;
    private TextView tvUserEmail;

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
        tvUserName = view.findViewById(R.id.tv_user_name);
        ivProfilePicture = view.findViewById(R.id.iv_profile_picture);
        tvUserEmail = view.findViewById(R.id.tv_user_email);

        // 2. Cargar datos del perfil si el token existe
        if (MainActivity.spotifyAccessToken != null) {
            loadUserProfileData();
        } else {
            // Mostrar estado desconectado
            tvUserName.setText("Invitado");
        }

        // 3. Implementar el Listener para Cerrar Sesión
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

    // ===========================================================
    // 👤 CARGA DE DATOS DEL PERFIL DE USUARIO
    // ===========================================================
    public void loadUserProfileData() {
        if (MainActivity.spotifyAccessToken == null) return;

        SpotifyService api = RetrofitClient.getClient().create(SpotifyService.class);

        api.getCurrentUserProfile("Bearer " + MainActivity.spotifyAccessToken)
                .enqueue(new Callback<SpotifyUserProfileResponse>() {
                    @Override
                    public void onResponse(Call<SpotifyUserProfileResponse> call,
                                           Response<SpotifyUserProfileResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            SpotifyUserProfileResponse profile = response.body();

                            // 1. Actualizar Nombre de Usuario
                            String name = profile.getDisplayName();
                            if (name != null && !name.isEmpty()) {
                                tvUserName.setText(name);
                            }

                            // 3. Actualizar Foto de Perfil
                            if (profile.getImages() != null && !profile.getImages().isEmpty()) {
                                // Tomamos la primera imagen
                                String imageUrl = profile.getImages().get(0).getUrl();

                                if (getContext() != null) {
                                    Glide.with(getContext())
                                            .load(imageUrl)
                                            .placeholder(R.drawable.image_2930)
                                            .error(R.drawable.image_2930)
                                            .into(ivProfilePicture);
                                }
                            }

                        } else {
                            Log.e(TAG, "❌ Error al cargar perfil API: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<SpotifyUserProfileResponse> call, Throwable t) {
                        Log.e(TAG, "❌ Fallo al cargar perfil: " + t.getMessage(), t);
                        tvUserName.setText("Error al cargar");
                    }
                });
    }

}


