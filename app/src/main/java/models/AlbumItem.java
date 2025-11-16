package models; // o api

import com.google.gson.annotations.SerializedName;
import models.Image;

import java.util.List;

public class AlbumItem {

    @SerializedName("id")
    private String id; // ID único del álbum en Spotify

    @SerializedName("name")
    private String name; // Nombre del álbum

    @SerializedName("uri")
    private String uri; // URI de Spotify para reproducción (ej: spotify:album:...)

    @SerializedName("album_type")
    private String albumType; // Tipo de álbum (album, single, compilation)

    @SerializedName("artists")
    private List<ArtistSimple> artists; // Lista de artistas principales

    @SerializedName("images")
    private List<Image> images; // Lista de carátulas del álbum (diferentes tamaños)

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

    public String getAlbumType() {
        return albumType;
    }

    public List<ArtistSimple> getArtists() {
        return artists;
    }

    public List<Image> getImages() {
        return images;
    }

}
