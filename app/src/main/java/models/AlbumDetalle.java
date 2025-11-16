package models;

public class AlbumDetalle {

    private final Cancion_Reciente track; // La información principal de la canción
    private final int trackNumber;       // El número de pista dentro del álbum


    public AlbumDetalle(Cancion_Reciente track, int trackNumber) {
        this.track = track;
        this.trackNumber = trackNumber;
    }

    // --- Getters ---
    public Cancion_Reciente getTrack() {
        return track;
    }

    public int getTrackNumber() {
        return trackNumber;
    }

    // Métodos de conveniencia para acceder rápidamente a datos de la canción
    public String getTitle() {
        return track.getTitulo();
    }

    public String getArtistName() {
        return track.getArtistaName();
    }

    public String getSpotifyUri() {
        return track.getSpotifyUri();
    }
}
