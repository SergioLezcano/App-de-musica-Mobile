package models;

public class ArtistExplorerItem {
    private final String id;
    private final String artistName;
    private final String imageUrl;

    public ArtistExplorerItem(String id, String artistName, String imageUrl) {
        this.id = id;
        this.artistName = artistName;
        this.imageUrl = imageUrl;
    }

    // Getters
    public String getId() { return id; }
    public String getArtistName() { return artistName; }
    public String getImageUrl() { return imageUrl; }
}