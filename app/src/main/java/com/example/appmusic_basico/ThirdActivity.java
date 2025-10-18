package com.example.appmusic_basico;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

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

        // ... (Otras inicializaciones)
    }
}
