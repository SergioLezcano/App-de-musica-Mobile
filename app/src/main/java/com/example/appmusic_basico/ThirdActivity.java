package com.example.appmusic_basico;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;


public class ThirdActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.music_play);

        // variable para el selector de iv_chevron_down
        ImageView minimizar = findViewById(R.id.iv_chevron_down);
        // Esto le indica a la MainActivity que debe mostrar el mini-reproductor al reanudarse.
        MainActivity.isMusicPlaying = true;

        minimizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ThirdActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        //menu opciones
        ImageButton btnMore_vert = findViewById(R.id.iv_more_vertical);

        btnMore_vert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(v); // Llama a la función para mostrar el menú
            }
        });

    }

    // Función central para manejar la lógica de mostrar y seleccionar el menú
    private void showPopupMenu(View view) {
        // 1. Crear una instancia de PopupMenu, anclándola al View (el ImageButton)
        PopupMenu popup = new PopupMenu(this, view);

        // 2. Inflar el menú (cargar las opciones del XML)
        popup.getMenuInflater().inflate(R.menu.menu_opciones_more_vert, popup.getMenu());

        // 3. Asignar el Listener para manejar la selección de opciones
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Lógica para manejar la acción
                return handleMenuItemSelection(item);
            }
        });

        // 4. Mostrar el menú
        popup.show();

    }
    //Metodo para manejar la Selección (Función de Acción)
    private boolean handleMenuItemSelection(MenuItem item) {
        int id = item.getItemId();

        // Uso de if/else para la lógica de selección
        if (id == R.id.opcion_favoritos) {
            Toast.makeText(this, "Acción: agregar a favoritos", Toast.LENGTH_SHORT).show();
            // Lógica para agregar a favoritos
            return true;
        } else if (id == R.id.opcion_play_list) {
            Toast.makeText(this, "Acción: agregar a playlist", Toast.LENGTH_SHORT).show();
            // Lógica para agregar a la playlist
            return true;
        } else if (id == R.id.opcion_fila) {
            Toast.makeText(this, "Acción: agregar a la fila", Toast.LENGTH_SHORT).show();
            // Lógica para agregar a la fila de reproduccion
            return true;
        } else if (id == R.id.opcion_album) {
            Toast.makeText(this, "Acción: ver album", Toast.LENGTH_SHORT).show();
            // Lógica para mostrar el album
            return true;
        } else if (id == R.id.opcion_artista) {
            Toast.makeText(this, "Acción: ver artista", Toast.LENGTH_SHORT).show();
            // Lógica para mostrar el/la artista
            return true;
        } else if (id == R.id.opcion_compartir) {
            Toast.makeText(this, "Acción: compartir", Toast.LENGTH_SHORT).show();
            // Lógica para compartir
            return true;
        } else if (id == R.id.opcion_ocultar) {
            Toast.makeText(this, "Acción: ocultar", Toast.LENGTH_SHORT).show();
            // Lógica para ocultar la canción
            return true;
        }

        return false; // Indica que la acción no fue manejada
    }
}
