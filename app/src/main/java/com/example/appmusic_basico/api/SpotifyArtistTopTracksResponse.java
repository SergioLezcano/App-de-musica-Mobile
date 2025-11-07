package com.example.appmusic_basico.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SpotifyArtistTopTracksResponse {

    @SerializedName("tracks")
    private List<Track> tracks;

    public List<Track> getTracks() {
        return tracks;
    }

    public static class Track {

        @SerializedName("name")
        private String name;

        @SerializedName("uri")
        private String uri;

        @SerializedName("preview_url")
        private String previewUrl;

        @SerializedName("duration_ms")
        private int durationMs;

        @SerializedName("artists")
        private List<Artist> artists;

        @SerializedName("album")
        private Album album;

        public String getName() { return name; }
        public String getUri() { return uri; }
        public String getPreviewUrl() { return previewUrl; }
        public int getDurationMs() { return durationMs; }
        public List<Artist> getArtists() { return artists; }
        public Album getAlbum() { return album; }

        // =====================
        // ARTISTA
        // =====================
        public static class Artist {
            @SerializedName("name")
            private String name;

            @SerializedName("id")
            private String id;

            public String getName() { return name; }
            public String getId() { return id; }
        }

        // =====================
        // ALBUM
        // =====================
        public static class Album {
            @SerializedName("name")
            private String name;

            @SerializedName("images")
            private List<Image> images;

            public String getName() { return name; }
            public List<Image> getImages() { return images; }

            public static class Image {
                @SerializedName("url")
                private String url;

                @SerializedName("width")
                private int width;

                @SerializedName("height")
                private int height;

                public String getUrl() { return url; }
                public int getWidth() { return width; }
                public int getHeight() { return height; }
            }
        }
    }
}

