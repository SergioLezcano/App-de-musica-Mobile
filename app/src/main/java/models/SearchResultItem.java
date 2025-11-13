package models;

// Modelo para manejar Artistas, Álbumes y Canciones de manera uniforme

public class SearchResultItem {

    private final String title;     // Nombre de la canción, álbum o artista
    private final String subtitle;  // Nombre del artista (o descripción, e.g., "Álbum")
    private final String imageUrl;  // URL de la imagen
    private final String spotifyId; // ID del objeto en Spotify
    private final String spotifyUri;// URI (usado para reproducción de canciones/álbumes)
    private final String type;      // CRÍTICO: "artist", "album", o "track"

    public SearchResultItem(String title, String subtitle, String imageUrl,
                            String spotifyId, String spotifyUri, String type) {
        this.title = title;
        this.subtitle = subtitle;
        this.imageUrl = imageUrl;
        this.spotifyId = spotifyId;
        this.spotifyUri = spotifyUri;
        this.type = type;
    }

    // --- Getters ---
    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public String getImageUrl() { return imageUrl; }
    public String getSpotifyId() { return spotifyId; }
    public String getSpotifyUri() { return spotifyUri; }
    public String getType() { return type; }
}