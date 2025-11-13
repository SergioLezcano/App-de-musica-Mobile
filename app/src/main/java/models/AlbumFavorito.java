package models;

public class AlbumFavorito {
    private String albumName;
    private String artistName;
    private String imageUrl;
    private String spotifyUri;
    private String spotifyId;

    // Constructor para el álbum actual en ThirdActivity
    public AlbumFavorito(String albumName, String artistName, String imageUrl, String spotifyUri, String spotifyId) {
        this.albumName = albumName;
        this.artistName = artistName;
        this.imageUrl = imageUrl;
        this.spotifyUri = spotifyUri;
        this.spotifyId = spotifyId;
    }

    // Getters y Setters
    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getSpotifyUri() {
        return spotifyUri;
    }

    public void setSpotifyUri(String spotifyUri) {
        this.spotifyUri = spotifyUri;
    }

    public String getSpotifyId() {
        return spotifyId;
    }

    public void setSpotifyId(String spotifyId) {
        this.spotifyId = spotifyId;
    }
}
