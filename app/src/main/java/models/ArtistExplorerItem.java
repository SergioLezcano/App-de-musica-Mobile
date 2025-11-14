package models;

public class ArtistExplorerItem {
    private final String id;
    private final String artistName;
    private final String imageUrl;
    private final String backgroundColorHex;

    public ArtistExplorerItem(String id, String artistName, String imageUrl, String backgroundColorHex) {
        this.id = id;
        this.artistName = artistName;
        this.imageUrl = imageUrl;
        this.backgroundColorHex = backgroundColorHex;
    }

    // Getters
    public String getId() { return id; }
    public String getArtistName() { return artistName; }
    public String getImageUrl() { return imageUrl; }
    public String getBackgroundColorHex() { return backgroundColorHex; }
}