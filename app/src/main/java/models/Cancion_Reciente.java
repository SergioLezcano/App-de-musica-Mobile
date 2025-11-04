package models;

public class Cancion_Reciente {
    private String titulo;
    private String artistaName;
    private String coverUrl; // URL de Spotify
    private String spotifyUri;
    private boolean fromSpotify;

    public Cancion_Reciente(String titulo, String artistaName, String coverUrl,  String spotifyUri, boolean fromSpotify) {
        this.titulo = titulo;
        this.artistaName = artistaName;
        this.coverUrl = coverUrl;
        this.spotifyUri = spotifyUri;
        this.fromSpotify = fromSpotify;
    }

    // Getters
    public String getTitulo() { return titulo; }
    public String getArtistaName() { return artistaName; }
    public String getCoverUrl() { return coverUrl; }
    public String getSpotifyUri() { return spotifyUri; }
    public boolean isFromSpotify() { return fromSpotify; }
}

