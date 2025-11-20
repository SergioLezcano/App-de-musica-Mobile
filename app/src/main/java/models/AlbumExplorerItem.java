package models;

public class AlbumExplorerItem {
    private final String id;
    private final String albumName;
    private final String artistName;
    private String artistId;
    private final String imageUrl;
    private final String backgroundColorHex;

    public AlbumExplorerItem(String id, String albumName, String artistName, String artistId, String imageUrl, String backgroundColorHex) {
        this.id = id;
        this.albumName = albumName;
        this.artistName = artistName;
        this.artistId = artistId;
        this.imageUrl = imageUrl;
        this.backgroundColorHex = backgroundColorHex;
    }

    // Getters
    public String getId() { return id; }
    public String getAlbumName() { return albumName; }
    public String getArtistName() { return artistName; }
    public String getImageUrl() { return imageUrl; }
    public String getArtistId(){return artistId; }
    public String getBackgroundColorHex() { return backgroundColorHex; }
}