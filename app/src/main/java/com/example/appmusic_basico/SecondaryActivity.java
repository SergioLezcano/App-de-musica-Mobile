package com.example.appmusic_basico;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

public class SecondaryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.music_list);

        ImageButton back_button = findViewById(R.id.btn_back);
        FrameLayout ic_play_2 = findViewById(R.id.play_button_layout);


        //Logica para volver a la pagina anterior
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //logica que pasa de SecondaryActivity a ThirdActivity
        ic_play_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SecondaryActivity.this, ThirdActivity.class);
                startActivity(intent);
            }
        });

        //menu opciones de la primera fila
        ImageButton btnMore_vert_1 = findViewById(R.id.iv_more_vertical_1);

        btnMore_vert_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(v); // Llama a la función para mostrar el menú
            }
        });

        //menu opciones de la segunda fila
        ImageButton btnMore_vert_2 = findViewById(R.id.iv_more_vertical_2);

        btnMore_vert_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(v); // Llama a la función para mostrar el menú
            }
        });

        //menu opciones de la segunda fila
        ImageButton btnMore_vert_3 = findViewById(R.id.iv_more_vertical_3);

        btnMore_vert_3.setOnClickListener(new View.OnClickListener() {
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
        popup.getMenuInflater().inflate(R.menu.menu_opciones_music_list, popup.getMenu());

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

    private boolean handleMenuItemSelection(MenuItem item) {
        int id = item.getItemId();

        // Uso de if/else para la lógica de selección
        if (id == R.id.opcion_reproducir) {
            Toast.makeText(this, "Acción: Reproducir música", Toast.LENGTH_SHORT).show();
            // Lógica de accion
            return true;
        } else if (id == R.id.opcion_agregar_favoritos) {
            Toast.makeText(this, "Acción: Agregar a favoritos", Toast.LENGTH_SHORT).show();
            // Lógica de accion requerida
            return true;
        } else if (id == R.id.opcion_agregar_a_lista) {
            Toast.makeText(this, "Acción: Agregar a la lista", Toast.LENGTH_SHORT).show();
            // Lógica de accion requerida
            return true;
        } else if (id == R.id.opcion_ocultar) {
            Toast.makeText(this, "Acción: Ocultar", Toast.LENGTH_SHORT).show();
            // Lógica de accion requerida
            return true;
        }

        return false; // Indica que la acción no fue manejada
    }
}
