package models;

public class AlbumExplorerItem {
    private final String id;
    private final String albumName;
    private final String artistName;
    private final String imageUrl;

    public AlbumExplorerItem(String id, String albumName, String artistName, String imageUrl) {
        this.id = id;
        this.albumName = albumName;
        this.artistName = artistName;
        this.imageUrl = imageUrl;
    }

    // Getters
    public String getId() { return id; }
    public String getAlbumName() { return albumName; }
    public String getArtistName() { return artistName; }
    public String getImageUrl() { return imageUrl; }
}