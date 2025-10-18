package com.example.appmusic_basico;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {
    private ConstraintLayout miniPlayerBar;
    // En la app real se debe gestionar mediante un service de musica

    // Usamos esto temporalmente para simular el miniPlayer:
    public static boolean isMusicPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ConstraintLayout nuevoLanzamineto = findViewById(R.id.container_nuevo_lanzamiento);
        RecyclerView favoritos = findViewById(R.id.rv_favorite_artists);
        miniPlayerBar = findViewById(R.id.mini_player_bar);
        FrameLayout play_button = findViewById(R.id.fl_play_button);

        //Configuracion del minPlayer
        miniPlayerBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Logica para reabrir el ThirdActivity (vista de reproduccion)
                Intent intent = new Intent(MainActivity.this, ThirdActivity.class);
                startActivity(intent);
            }
        });

        // Seccion para click listener ConstraintLayout de nuevo lanzamiento

        nuevoLanzamineto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SecondaryActivity.class);
                startActivity(intent);
            }
        });

        favoritos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SecondaryActivity.class);
                startActivity(intent);
            }
        });
        // Logica para que el boton play del la seccion nuevos lanzamientos lleve al ThirdActivity
        play_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ThirdActivity.class);
                startActivity(intent);
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        // Cada vez que la MainActivity vuelve a ser visible,
        // verifica si la música debería seguir sonando.
        updateMiniPlayerVisibility();
    }

    private void updateMiniPlayerVisibility() {
        if (isMusicPlaying) {
            // Si la música está activa, muestra el mini-reproductor
            miniPlayerBar.setVisibility(View.VISIBLE);
        } else {
            // Si la música se detuvo, oculta el mini-reproductor
            miniPlayerBar.setVisibility(View.GONE);
        }
    }
}