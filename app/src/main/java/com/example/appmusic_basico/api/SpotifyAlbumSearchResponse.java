package com.example.appmusic_basico.api;

import java.util.List;

public class SpotifyAlbumSearchResponse {

    // El objeto raíz de la respuesta de búsqueda de álbumes contiene el campo "albums"
    private Albums albums;

    public Albums getAlbums() {
        return albums;
    }

    // --- Clase interna para el contenedor de álbumes ---
    public static class Albums {
        private List<Item> items; // La lista real de resultados de álbumes

        public List<Item> getItems() {
            return items;
        }
    }

    // --- Clase interna para cada resultado de álbum individual ---
    public static class Item {
        private String id;
        private String name;
        private String uri;
        private String album_type;
        private List<Artist> artists; // Lista de artistas del álbum
        private List<Image> images;  // Lista de imágenes del álbum (carátulas)

        public String getId() { return id; }
        public String getName() { return name; }
        public String getUri() { return uri; }
        public String getAlbumType() { return album_type; }
        public List<Artist> getArtists() { return artists; }
        public List<Image> getImages() { return images; }
    }

    // --- Clases de soporte (pueden ser reutilizadas si ya existen) ---

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