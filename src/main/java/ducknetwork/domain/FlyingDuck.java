package ducknetwork.domain;

public class FlyingDuck extends Duck implements Zburator {
    public FlyingDuck(Long id, String username, String email, String password, double speed, double endurance) {
        super(id, username, email, password, speed, endurance);
    }

    @Override
    public void zboara() {
        System.out.println(getUsername() + " is flying!");
    }
}
