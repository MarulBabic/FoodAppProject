package my.food.foodapp.Domain;

public class Category {
    private Long Id;
    private String ImagePath;
    private String Name;

    public Category() {
    }

    public Long getId() {
        return Id;
    }

    public void setId(long id) {
        this.Id = id;
    }

    public String getImagePath() {
        return ImagePath;
    }

    public void setImagePath(String imagePath) {
        ImagePath = imagePath;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }
}
