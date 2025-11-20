package ducknetwork.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Cârd (group) of ducks.
 */
public class Card {
    private final Long id;
    private final String name;
    private final List<Duck> members = new ArrayList<>();

    public Card(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public List<Duck> getMembers() { return members; }

    public void addDuck(Duck d) {
        if (d == null) return;
        if (!members.contains(d)) {
            members.add(d);
            d.setCard(this);
        }
    }

    public void removeDuck(Duck d) {
        if (members.remove(d)) {
            if (d.getCard() == this) d.setCard(null);
        }
    }

    public double getPerformantaMedie() {
        if (members.isEmpty()) return 0.0;
        double sum = 0.0;
        for (Duck d : members) sum += (d.getSpeed() + d.getEndurance()) / 2.0;
        return sum / members.size();
    }

    @Override
    public String toString() {
        return String.format("Card{id=%d, name=%s, members=%d}", id, name, members.size());
    }
}
