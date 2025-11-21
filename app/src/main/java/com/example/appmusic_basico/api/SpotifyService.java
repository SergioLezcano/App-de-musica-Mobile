package com.example.appmusic_basico.api;

import models.ArtistsResponse;
import models.NewReleasesResponse;
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

    @GET("browse/categories/{category_id}/playlists")
    Call<CategoryPlaylistResponse> getCategoryPlaylists(
            @Header("Authorization") String authHeader,
            @Path("category_id") String categoryId,
            @Query("country") String country,
            @Query("limit") int limit
    );

    @GET("browse/categories")
    Call<SpotifyCategoriesResponse> getAllCategories(
            @Header("Authorization") String token,
            @Query("country") String country,
            @Query("limit") int limit
    );

    @GET("browse/new-releases")
    Call<NewReleasesResponse> getNewReleases(
            @Header("Authorization") String authorization,
            @Query("country") String country,
            @Query("limit") int limit
    );

    // Endpoint para obtener la información de MÚLTIPLES artistas
    @GET("artists")
    Call<ArtistsResponse> getMultipleArtists(
            @Header("Authorization") String authHeader,
            @Query("ids") String artistIds // Ej: "artistId1,artistId2,artistId3"
    );

    @GET("me")
    Call<SpotifyUserProfileResponse> getCurrentUserProfile(
            @Header("Authorization") String authorization
    );
}
