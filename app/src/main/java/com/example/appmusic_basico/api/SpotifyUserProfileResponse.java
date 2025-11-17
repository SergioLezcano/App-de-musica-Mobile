package com.example.appmusic_basico.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SpotifyUserProfileResponse {
    @SerializedName("display_name")
    private String displayName;

    @SerializedName("id")
    private String id;

    @SerializedName("images")
    private List<Image> images;

    // Getters
    public String getDisplayName() { return displayName; }
    public String getId() { return id; }
    public List<Image> getImages() { return images; }

    // Clase interna para la imagen
    public static class Image {
        @SerializedName("url")
        private String url;

        public String getUrl() { return url; }
    }
}
