package my.food.foodapp.Activity;

public class UserDataSingleton {
    private static UserDataSingleton instance;
    private String firstName;
    private String lastName;

    private UserDataSingleton(){}

    public static UserDataSingleton getInstance(){
        if(instance == null){
            instance = new UserDataSingleton();
        }
        return instance;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
