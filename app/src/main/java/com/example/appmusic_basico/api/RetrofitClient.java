package com.example.appmusic_basico.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static Retrofit retrofit = null;
    private static final String BASE_URL = "https://api.spotify.com/v1/";

    public static Retrofit getClient() {
        if (retrofit == null) {
            // 💡 1. Crear el interceptor de logs
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY); // Mostrar la URL, headers y body

            // 💡 2. Crear el cliente OkHttp y añadir el interceptor
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging) // Añadir el interceptor
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client) // ⬅️ Usar el cliente con el interceptor
                    .build();
        }
        return retrofit;
    }
}

