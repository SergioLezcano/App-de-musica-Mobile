package com.example.appmusic_basico.api;

import java.util.List;

public class SpotifyRecentlyPlayedResponse {

    private List<Item> items;

    public List<Item> getItems() {
        return items;
    }

    public static class Item {
        private Track track;

        public Track getTrack() {
            return track;
        }
    }

    public static class Track {
        private String name;
        private String uri;
        private Album album; // Cambiar de String a Album
        private List<Artist> artists;

        public String getName() { return name; }
        public List<Artist> getArtists() { return artists; }
        public String getUri() { return uri; }
        public Album getAlbum() { return album; }

        public static class Album {
            private List<Image> images;
            public List<Image> getImages() { return images; }

            public static class Image {
                private String url;
                public String getUrl() { return url; }
            }
        }
    }

    public static class Artist {
        private String name;

        public String getName() { return name; }
    }
}
