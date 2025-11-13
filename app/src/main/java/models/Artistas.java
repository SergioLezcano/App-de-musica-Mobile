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
        // CRÍTICO: Comparar por nombre, asegurando que ambos no sean null
        return nombre != null && nombre.equalsIgnoreCase(artista.nombre);
    }

    /**
     * Genera un hashCode basado únicamente en el nombre.
     */
    @Override
    public int hashCode() {
        // CRÍTICO: Usar el nombre en minúsculas para consistencia con equals
        return nombre != null ? nombre.toLowerCase().hashCode() : 0;
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