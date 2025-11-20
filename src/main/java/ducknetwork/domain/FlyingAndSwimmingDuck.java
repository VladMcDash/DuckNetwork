package ducknetwork.domain;

public class FlyingAndSwimmingDuck extends Duck implements Zburator, Inotator {
    public FlyingAndSwimmingDuck(Long id, String username, String email, String password, double speed, double endurance) {
        super(id, username, email, password, speed, endurance);
    }

    @Override
    public void zboara() {
        System.out.println(getUsername() + " is flying and training!");
    }

    @Override
    public void inoata() {
        System.out.println(getUsername() + " is swimming and training!");
    }
}
