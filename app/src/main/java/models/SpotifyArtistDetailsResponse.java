package models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SpotifyArtistDetailsResponse {

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("images")
    private List<Image> images;

    public String getId() { return id; }
    public String getName() { return name; }
    public List<Image> getImages() { return images; }

    public static class Image {
        @SerializedName("url")
        private String url;

        public String getUrl() { return url; }
    }
}

