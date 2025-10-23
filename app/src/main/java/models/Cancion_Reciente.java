package models;

// Ejemplo básico del modelo
public class Cancion_Reciente {
    private String titulo_cancion;
    private String artista_name;
    private int resourceId;

    public Cancion_Reciente(String titulo_cancion, String artista_name, int resourceId) {
        this.titulo_cancion = titulo_cancion;
        this.artista_name = artista_name;
        this.resourceId = resourceId;
    }

    // Getters
        public String getTitulo() { return titulo_cancion; }
        public String getArtistaName(){return artista_name;}
        public int getResourceId() { return resourceId; }

    //Setters
    public void setArtista_name(String artista_name){
        this.artista_name = artista_name;
    }

    public void setTitulo_cancion(String titulo_cancion){
        this.titulo_cancion = titulo_cancion;
    }

    public void setResourceId(int resourceId){
        this.resourceId = resourceId;
    }
}

