package my.food.foodapp.Domain;

public class Time {
    private Long Id;
    private String Value;

    public Time() {
    }

    @Override
    public String toString() {
        return Value;
    }

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public String getValue() {
        return Value;
    }

    public void setValue(String value) {
        Value = value;
    }
}
