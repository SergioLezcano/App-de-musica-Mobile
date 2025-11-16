package models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ArtistsResponse {

    // La respuesta contiene una lista de objetos de artista bajo la clave "artists"
    @SerializedName("artists")
    private List<ArtistItem> artists;

    public List<ArtistItem> getArtists() {
        return artists;
    }

    // Nota: Para este endpoint, Spotify no suele devolver metadatos adicionales como 'href' o 'limit'
    // fuera de la lista principal, por lo que la estructura es simple.
}
