package models;

import com.google.gson.annotations.SerializedName;

public class Image {

    @SerializedName("url")
    private String url; // URL de la imagen

    @SerializedName("height")
    private int height; // Altura de la imagen en píxeles

    @SerializedName("width")
    private int width; // Ancho de la imagen en píxeles

    // --- Getters ---
    public String getUrl() {
        return url;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }
}