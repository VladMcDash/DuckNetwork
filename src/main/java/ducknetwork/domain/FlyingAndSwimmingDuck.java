package ducknetwork.domain;

public class FlyingAndSwimmingDuck extends Duck {
    public FlyingAndSwimmingDuck(Long id, String username, String email, String password,
                                 double speed, double endurance) {
        super(id, username, email, password, speed, endurance, "FLYING_AND_SWIMMING");
    }
}
