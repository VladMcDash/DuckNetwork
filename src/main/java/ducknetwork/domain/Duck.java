package ducknetwork.domain;

/**
 * Abstract Duck class. Concrete duck types implement Inotator and/or Zburator.
 */
public abstract class Duck extends User {

    protected double speed;
    protected double endurance;
    protected String type;
    protected Card card;

    public Duck(Long id, String username, String email, String password,
                double speed, double endurance, String type) {
        super(id, username, email, password);
        this.speed = speed;
        this.endurance = endurance;
        this.type = type;
    }

    public double getSpeed() { return speed; }
    public double getEndurance() { return endurance; }

    public String getType() { return type; }

    public Card getCard() { return card; }
    public void setCard(Card c) { this.card = c; }
    @Override
    public String toString() {
        return String.format("%s{id=%d, username=%s, speed=%.2f, endurance=%.2f}", getClass().getSimpleName(), id, username, speed, endurance);
    }
}

