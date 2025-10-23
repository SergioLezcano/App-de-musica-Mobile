package models;

public class Artistas {

    private String artista_name;
    private int imagenResourceId;

    public Artistas(String artista_name, int imageResourceId){
        this.artista_name = artista_name;
        this.imagenResourceId = imagenResourceId;

    }

    //Getters
    public String getArtista() {return artista_name;}
    public int getImagenResourceId() { return imagenResourceId; }

    //Setters

    public void setArtista_name(String artista_name) {
        this.artista_name = artista_name;
    }

    public void setImagenResourceId(int imagenResourceId){
        this.imagenResourceId = imagenResourceId;
    }
}
