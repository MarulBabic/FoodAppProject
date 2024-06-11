package my.food.foodapp.Domain;

public class Location {
    private Long Id;
    private String loc;

    @Override
    public String toString() {
        return loc;
    }

    public Location() {
    }

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public String getLoc() {
        return loc;
    }

    public void setLoc(String loc) {
        this.loc = loc;
    }
}
