package com.example.appmusic_basico;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class SecondaryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.music_list);

        ImageView back_button = findViewById(R.id.iv_boton_atras);
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
    }
}
