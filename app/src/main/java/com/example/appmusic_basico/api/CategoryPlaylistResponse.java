package com.example.appmusic_basico.api; // o .api.responses

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CategoryPlaylistResponse {

    @SerializedName("playlists")
    private Playlists playlists;

    public Playlists getPlaylists() {
        return playlists;
    }

    // -------------------------------------------------------------------
    // 1. Contenedor Principal de Playlists
    // -------------------------------------------------------------------
    public static class Playlists {

        @SerializedName("href")
        private String href;

        @SerializedName("items")
        private List<Item> items; // La lista de playlists

        @SerializedName("limit")
        private int limit;

        @SerializedName("offset")
        private int offset;

        @SerializedName("total")
        private int total;

        public List<Item> getItems() {
            return items;
        }

        // -------------------------------------------------------------------
        // 2. Resultado de Playlist Individual (Item)
        // -------------------------------------------------------------------
        public static class Item {

            @SerializedName("id")
            private String id; // El ID de Spotify (clave)

            @SerializedName("uri")
            private String uri; // La URI de Spotify (ej. spotify:playlist:...)

            @SerializedName("name")
            private String name; // El nombre de la playlist (Título)

            @SerializedName("description")
            private String description;

            @SerializedName("images")
            private List<Image> images; // Lista de imágenes (portadas)

            @SerializedName("owner")
            private Owner owner; // Información del creador/dueño

            // --- Getters ---
            public String getId() { return id; }
            public String getName() { return name; }
            public String getUri() { return uri; }
            public List<Image> getImages() { return images; }
            public Owner getOwner() { return owner; }
        }
    }

    // -------------------------------------------------------------------
    // 3. Clases de Datos Auxiliares (Owner, Image)
    // -------------------------------------------------------------------

    public static class Owner {
        @SerializedName("display_name")
        private String displayName; // Nombre visible del creador
        @SerializedName("id")
        private String id;

        public String getDisplayName() { return displayName; }
        public String getId() { return id; }
    }

    // ⚠️ Esta clase se define internamente asumiendo que omitiste la clase Image.java global.
    public static class Image {
        @SerializedName("url")
        private String url; // La URL de la portada
        @SerializedName("height")
        private Integer height;
        @SerializedName("width")
        private Integer width;

        public String getUrl() { return url; }
    }
}