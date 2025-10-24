package com.example.appmusic_basico;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appmusic_basico.FragmentHome;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNav;
    private ConstraintLayout miniPlayerBar;
    // En la app real se debe gestionar mediante un service de musica

    // Usamos esto temporalmente para simular el miniPlayer:
    public static boolean isMusicPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // 1. Obtener la referencia al BottomNavigationView
        bottomNav = findViewById(R.id.bottom_navigation_bar);

        // 2. Establecer el listener para la navegación
        bottomNav.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {

            //Metodo publico para navegar por los item del menu
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();

                // Usamos if/else if ya que Java no soporta switch con R.id.
                if (itemId == R.id.nav_home) {
                    selectedFragment = new FragmentHome();
                } else if (itemId == R.id.nav_search) {
                    selectedFragment = new FragmentSearch();
                } else if (itemId == R.id.nav_favorite) {
                    selectedFragment = new FragmentFavourite();
                } else if (itemId == R.id.nav_profile) {
                    selectedFragment = new FragmentProfile();
                }

                // if para cargar el Fragment
                if (selectedFragment != null) {
                    loadFragment(selectedFragment);
                    return true; // Indica que la selección fue manejada
                }
                return false;
            }
        });

        // 3. Cargar el fragmento inicial al iniciar la actividad
        // Verifica si ya hay un estado guardado (evita recargar tras rotación)
        if (savedInstanceState == null) {
            Fragment initialFragment = new FragmentHome();
            loadFragment(initialFragment);
            bottomNav.setSelectedItemId(R.id.nav_home);
        }


        miniPlayerBar = findViewById(R.id.mini_player_bar);

        //Configuracion del minPlayer
        miniPlayerBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Logica para reabrir el ThirdActivity (vista de reproduccion)
                Intent intent = new Intent(MainActivity.this, ThirdActivity.class);
                // Si ya está en la pila, la trae al frente sin recrearla
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        });

        // Llama para establecer la visibilidad inicial
        updateMiniPlayerVisibility();

    }


    //Metodo para la navegacion del menu principal
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }

    // 2. Método para navegación interna de la app
    public void navigateToFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .addToBackStack(null) // GUARDA el fragmento anterior
                .commit();
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