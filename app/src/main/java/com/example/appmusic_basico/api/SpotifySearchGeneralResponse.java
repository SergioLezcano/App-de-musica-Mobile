package com.example.appmusic_basico.api;

import com.example.appmusic_basico.api.SpotifyArtistSearchResponse.Artists;

import com.example.appmusic_basico.api.SpotifyAlbumSearchResponse.Albums;

import com.example.appmusic_basico.api.SpotifyTrackSearchResponse.Tracks;

public class SpotifySearchGeneralResponse {

    // Estos campos mapean la estructura principal del JSON de búsqueda
    private Tracks tracks;
    private Artists artists;
    private Albums albums;

    public Tracks getTracks() { return tracks; }
    public Artists getArtists() { return artists; }
    public Albums getAlbums() { return albums; }

    // NOTA: Debes asegurar que las clases internas (Tracks, Albums, Artists)
    // y sus Item's correspondientes estén definidas con los campos correctos.
}
