package com.example.appmusic_basico.api; // o donde guardes tus respuestas

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SpotifyCategoriesResponse {

    @SerializedName("categories")
    private Categories categories;

    public Categories getCategories() {
        return categories;
    }

    // -------------------------------------------------------------------
    // 1. Contenedor Principal de Categorías
    // -------------------------------------------------------------------
    public static class Categories {

        @SerializedName("href")
        private String href;

        @SerializedName("items")
        private List<Item> items; // La lista de categorías (lo que nos interesa)

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
        // 2. Categoría Individual (Item)
        // -------------------------------------------------------------------
        public static class Item {

            @SerializedName("href")
            private String href;

            @SerializedName("id")
            private String id; // ⬅️ ¡El ID que necesitas para la verificación!

            @SerializedName("icons")
            private List<Image> icons; // Lista de íconos (imágenes de la tarjeta)

            @SerializedName("name")
            private String name; // Nombre de la categoría (ej: "Rock")

            // --- Getters ---
            public String getId() { return id; }
            public List<Image> getIcons() { return icons; }
            public String getName() { return name; }
        }
    }

    // -------------------------------------------------------------------
    // 3. Clase de Imagen (Ícono de Categoría)
    // -------------------------------------------------------------------

    // ⚠️ Definición interna de Image, ya que no se usa la clase global.
    public static class Image {
        @SerializedName("url")
        private String url;
        @SerializedName("height")
        private Integer height;
        @SerializedName("width")
        private Integer width;

        public String getUrl() { return url; }
    }
}
