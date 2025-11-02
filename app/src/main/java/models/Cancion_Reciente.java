package models;

public class Cancion_Reciente {

    private String titulo_cancion;
    private String artista_name;
    private int coverResourceId;     // Para imágenes locales (R.drawable.*)
    private int songResourceId;      // Para audios locales (R.raw.*)
    private String coverUrl;         // Para imágenes remotas (Spotify)
    private String previewUrl;       // Para reproducción desde URL (Spotify)
    private boolean isFromSpotify;   // Indica si la canción viene de Spotify o es local

    // --- Constructores ---

    // Constructor para canciones locales
    public Cancion_Reciente(String titulo_cancion, String artista_name, int coverResourceId, int songResourceId) {
        this.titulo_cancion = titulo_cancion;
        this.artista_name = artista_name;
        this.coverResourceId = coverResourceId;
        this.songResourceId = songResourceId;
        this.isFromSpotify = false;
    }

    // Constructor para canciones de Spotify (remotas)
    public Cancion_Reciente(String titulo_cancion, String artista_name, String coverUrl, String previewUrl) {
        this.titulo_cancion = titulo_cancion;
        this.artista_name = artista_name;
        this.coverUrl = coverUrl;
        this.previewUrl = previewUrl;
        this.isFromSpotify = true;
    }

    // --- Getters ---
    public String getTitulo() { return titulo_cancion; }
    public String getArtistaName() { return artista_name; }
    public int getCoverResourceId() { return coverResourceId; }
    public int getSongResourceId() { return songResourceId; }
    public String getCoverUrl() { return coverUrl; }
    public String getPreviewUrl() { return previewUrl; }
    public boolean isFromSpotify() { return isFromSpotify; }

    // --- Setters opcionales ---
    public void setTitulo_cancion(String titulo_cancion) { this.titulo_cancion = titulo_cancion; }
    public void setArtista_name(String artista_name) { this.artista_name = artista_name; }
    public void setCoverResourceId(int coverResourceId) { this.coverResourceId = coverResourceId; }
    public void setSongResourceId(int songResourceId) { this.songResourceId = songResourceId; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public void setPreviewUrl(String previewUrl) { this.previewUrl = previewUrl; }
}
