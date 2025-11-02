package com.example.appmusic_basico.api;

import models.Cancion_Reciente;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface SpotifyService {
    //Endpoint de Spotify: GET https://api.spotify.com/v1/me/player/recently-played
    @GET("me/player/recently-played?limit=10")
    Call<SpotifyRecentlyPlayedResponse> getRecentlyPlayed(
            @Header("Authorization") String authHeader
    );
}

