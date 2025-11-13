package models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SpotifyAlbumDetailsResponse {

    @SerializedName("id")
    private String id;

    @SerializedName("images")
    private List<Image> images;

    // Getters
    public String getId() { return id; }
    public List<Image> getImages() { return images; }

    public static class Image {
        @SerializedName("url")
        private String url;

        // Getter
        public String getUrl() { return url; }
    }
}