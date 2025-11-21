package com.example.appmusic_basico.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SpotifyUserProfileResponse {

    @SerializedName("display_name")
    private String displayName;

    private String id;

    @SerializedName("email")
    private String email;
    private List<ImageObject> images;

    public String getDisplayName() { return displayName; }
    public String getId() { return id; }
    public String getEmail() { return email; }
    public List<ImageObject> getImages() { return images; }

    public static class ImageObject {
        private String url;
        public String getUrl() { return url; }
    }
}
