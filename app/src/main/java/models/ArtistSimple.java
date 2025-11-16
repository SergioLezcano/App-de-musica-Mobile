package models; // o api

import com.google.gson.annotations.SerializedName;

public class ArtistSimple {

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name; // Nombre del artista

    // --- Getters ---
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}