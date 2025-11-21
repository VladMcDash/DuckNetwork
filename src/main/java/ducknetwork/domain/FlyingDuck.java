package ducknetwork.domain;

public class FlyingDuck extends Duck {
    public FlyingDuck(Long id, String username, String email, String password,
                      double speed, double endurance) {
        super(id, username, email, password, speed, endurance, "FLYING");
    }
}

