package models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ArtistsResponse {

    @SerializedName("artists")
    private List<ArtistItem> artists;

    public List<ArtistItem> getArtists() {
        return artists;
    }

}
