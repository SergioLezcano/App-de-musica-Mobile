package models;

public class CategoryItem {
    private final String id;
    private final String name;
    private final String imageUrl;

    public CategoryItem(String id, String name, String imageUrl) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getImageUrl() { return imageUrl; }
}