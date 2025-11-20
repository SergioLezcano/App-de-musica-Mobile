package com.example.appmusic_basico.api;

import java.util.List;

public class SpotifyAlbumTracksResponse {
    private List<Item> items;

    // Campos de paginación
    private int total;
    private String href;
    private String next;
    private int limit;
    private int offset;

    public List<Item> getItems() {
        return items;
    }

    public int getTotal() {
        return total;
    }

    public static class Item {
        private String id;
        private String name;
        private String uri; // URI de la pista, crucial para la reproducción
        private int track_number; // El número de la pista en el álbum
        private int duration_ms;  // Duración en milisegundos
        private boolean explicit; // Si tiene contenido explícito
        private List<Artist> artists; // Artistas de la pista (puede ser diferente al álbum)

        // El resto de los getters...
        public String getId() { return id; }
        public String getName() { return name; }
        public String getUri() { return uri; }
        public int getTrackNumber() { return track_number; }
        public int getDurationMs() { return duration_ms; }
        public boolean isExplicit() { return explicit; }
        public List<Artist> getArtists() { return artists; }
    }

    // --- Clase de soporte Artist (Puede ser reutilizada de otros modelos) ---
    public static class Artist {
        private String name;
        private String id;

        public String getName() { return name; }
        public String getId() { return id; }
    }
}
