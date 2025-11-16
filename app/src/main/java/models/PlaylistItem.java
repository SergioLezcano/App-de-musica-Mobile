package models;

public class PlaylistItem {
    private String id;
    private String title;
    private String artist;
    private String coverUrl; // URL para la imagen de la portada

    // Constructor completo para inicializar todos los campos
    public PlaylistItem(String id, String title, String artist, String coverUrl) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.coverUrl = coverUrl;
    }

    // --- Getters ---

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    // --- Setters (Opcionales, pero buena práctica si los datos pueden cambiar) ---
    /*
    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }
    */
}