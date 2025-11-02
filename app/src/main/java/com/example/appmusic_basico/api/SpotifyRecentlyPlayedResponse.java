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
        private List<Artist> artists;

        public String getName() {
            return name;
        }

        public List<Artist> getArtists() {
            return artists;
        }
    }

    public static class Artist {
        private String name;

        public String getName() {
            return name;
        }
    }
}

