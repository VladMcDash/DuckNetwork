package ducknetwork.domain;

public class SwimmingDuck extends Duck implements Inotator {
    public SwimmingDuck(Long id, String username, String email, String password, double speed, double endurance) {
        super(id, username, email, password, speed, endurance);
    }

    @Override
    public void inoata() {
        System.out.println(getUsername() + " is swimming at speed " + getSpeed());
    }
}
