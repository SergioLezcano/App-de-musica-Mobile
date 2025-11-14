package com.example.appmusic_basico.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;
import models.SpotifyAlbumDetailsResponse;

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

    @GET("artists/{id}/top-tracks")
    Call<SpotifyArtistTopTracksResponse> getArtistTopTracks(
            @Header("Authorization") String authHeader,
            @Path("id") String artistId,
            @Query("market") String market
    );

    @GET("albums/{id}")
    Call<SpotifyAlbumDetailsResponse> getAlbumDetails(
            @Header("Authorization") String authorization,
            @Path("id") String albumId
    );

    // 🆕 NUEVO MÉTODO: Búsqueda general por tipo (ej: "track,artist,album")
    @GET("search")
    Call<SpotifySearchGeneralResponse> searchAll(
            @Header("Authorization") String authHeader,
            @Query("q") String query,
            @Query("type") String type
    );

    @GET("albums/{id}/tracks")
    Call<SpotifyAlbumTracksResponse> getAlbumTracks(
            @Header("Authorization") String authHeader,
            @Path("id") String albumId
    );
}
