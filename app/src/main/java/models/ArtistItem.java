package models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ArtistItem {

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("uri")
    private String uri;

    @SerializedName("genres")
    private List<String> genres; // Lista de géneros asociados al artista

    @SerializedName("images")
    private List<Image> images; // Reutilizamos la clase Image existente

    @SerializedName("popularity")
    private int popularity; // Puntuación de popularidad de Spotify (0-100)

    // --- Getters ---
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUri() {
        return uri;
    }

    public List<String> getGenres() {
        return genres;
    }

    public List<Image> getImages() {
        return images;
    }

    public int getPopularity() {
        return popularity;
    }
}
