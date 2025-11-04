package com.example.appmusic_basico.api;

import java.util.List;

public class SpotifyArtistSearchResponse {

    private Artists artists;

    public Artists getArtists() {
        return artists;
    }

    public static class Artists {
        private List<Item> items;
        public List<Item> getItems() { return items; }
    }

    public static class Item {
        private String id;
        private String name;
        private List<Image> images;

        public String getId() { return id; }
        public String getName() { return name; }
        public List<Image> getImages() { return images; }
    }

    public static class Image {
        private String url;
        public String getUrl() { return url; }
    }
}

