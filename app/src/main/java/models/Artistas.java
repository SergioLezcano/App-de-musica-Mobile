package models;

import androidx.annotation.NonNull;


public class Artistas {

    private String nombre;
    private String imagenUrl; // URL de la imagen obtenida de Spotify
    private String idSpotify; // 🆔 ID real del artista en Spotify

    public Artistas(String nombre, String imagenUrl, String idSpotify) {
        this.nombre = nombre;
        this.imagenUrl = imagenUrl;
        this.idSpotify = idSpotify;
    }

    // --- Getters ---

    public String getNombre() {
        return nombre;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }
    public String getIdSpotify() {
        return idSpotify;
    }

    // --- Setters (Opcional) ---

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }
    public void setIdSpotify(String idSpotify) {
        this.idSpotify = idSpotify;
    }

    // --- Implementación para unicidad y comparación (CRUCIAL para Sets/Listas) ---

    /**
     * Dos artistas son iguales si tienen el mismo nombre.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Artistas artista = (Artistas) o;
        // Solo comparamos por el nombre del artista (el identificador único)
        return idSpotify != null && idSpotify.equals(artista.idSpotify);
    }

    /**
     * Genera un hashCode basado únicamente en el nombre.
     */
    @Override
    public int hashCode() {
        return nombre.hashCode();
    }

    @NonNull
    @Override
    public String toString() {
        return "Artista{" +
                "idSpotify='" + idSpotify + '\'' +
                ", nombre='" + nombre + '\'' +
                ", imagenUrl='" + imagenUrl + '\'' +
                '}';
    }
}