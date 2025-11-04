package com.example.appmusic_basico.api;

import java.util.List;

public class SpotifyArtistDetailsResponse {
    private List<Image> images;

    public List<Image> getImages() {
        return images;
    }

    public static class Image {
        private String url;
        public String getUrl() { return url; }
    }
}

