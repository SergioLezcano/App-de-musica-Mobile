package com.example.appmusic_basico.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

// 🚀 Interfaz para las llamadas a la API Web de Spotify
public interface SpotifyService {

    // Obtener canciones reproducidas recientemente
    @GET("me/player/recently-played?limit=20")
    Call<SpotifyRecentlyPlayedResponse> getRecentlyPlayed(
            @Header("Authorization") String authHeader
    );

    // Buscar artistas por nombre
    @GET("search")
    Call<SpotifyArtistSearchResponse> searchArtists(
            @Header("Authorization") String authHeader,
            @Query("q") String query,
            @Query("type") String type
    );
}
