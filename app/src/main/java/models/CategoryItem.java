package models;

public class CategoryItem {
    private final String id;
    private final String name;
    private final String imageUrl;
    private final String backgroundColorHex;

    public CategoryItem(String id, String name, String imageUrl, String backgroundColorHex) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.backgroundColorHex = backgroundColorHex;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getImageUrl() { return imageUrl; }
    public String getBackgroundColorHex() { return backgroundColorHex; }
}