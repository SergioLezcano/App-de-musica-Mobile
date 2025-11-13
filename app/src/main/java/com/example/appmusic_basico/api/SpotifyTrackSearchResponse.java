//Este modelo mapea la sección de pistas (tracks) de la respuesta de búsqueda de Spotify (/search?type=track,...).

package com.example.appmusic_basico.api;

import java.util.List;

public class SpotifyTrackSearchResponse {

    private Tracks tracks;

    public Tracks getTracks() {
        return tracks;
    }

    // --- Contenedor de Pistas ---
    public static class Tracks {
        private List<Item> items;

        public List<Item> getItems() {
            return items;
        }
    }

    // --- Resultado de Pista Individual ---
    public static class Item {
        private String id;
        private String name;
        private String uri; // URI de la canción para reproducción
        private Album album;
        private List<Artist> artists;

        public String getId() { return id; }
        public String getName() { return name; }
        public String getUri() { return uri; }
        public Album getAlbum() { return album; }
        public List<Artist> getArtists() { return artists; }
    }

    // --- Clases de Soporte ---

    public static class Album {
        private String id;
        private String name;
        private List<Image> images; // Carátula del álbum/canción

        public List<Image> getImages() { return images; }
        public String getId() { return id; }
        public String getName() { return name; }
    }

    public static class Artist {
        private String name;
        private String id;

        public String getName() { return name; }
        public String getId() { return id; }
    }

    public static class Image {
        private String url;

        public String getUrl() { return url; }
    }
}
