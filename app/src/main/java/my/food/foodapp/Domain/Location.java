package my.food.foodapp.Domain;

public class Location {
    private int Id;
    private String loc;

    @Override
    public String toString() {
        return loc;
    }

    public Location() {
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getLoc() {
        return loc;
    }

    public void setLoc(String loc) {
        this.loc = loc;
    }
}
