package models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class NewReleasesResponse {

    @SerializedName("albums")
    private Albums albums;

    // Getters y Setters
    public Albums getAlbums() { return albums; }

    public static class Albums {
        @SerializedName("items")
        private List<AlbumItem> items;

        // Getters y Setters
        public List<AlbumItem> getItems() { return items; }
    }
}