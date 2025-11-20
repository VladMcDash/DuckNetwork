package ducknetwork.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Observer-style Event: users can subscribe / unsubscribe and will be notified.
 */
public class Event {
    protected final Long id;
    protected final String name;
    protected final List<User> subscribers = new ArrayList<>();

    public Event(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() { return id; }
    public String getName() { return name; }

    public void subscribe(User u) {
        if (u == null) return;
        if (!subscribers.contains(u)) subscribers.add(u);
    }

    public void unsubscribe(User u) {
        subscribers.remove(u);
    }

    public List<User> getSubscribers() {
        return subscribers;
    }

    /**
     * Notify all subscribers with a textual message. Default behaviour: call log on user (not implemented here),
     * we keep it simple and print to console and call receiveMessage only if user is a Person (optional).
     */
    public void notifySubscribers(String message) {
        for (User u : new ArrayList<>(subscribers)) {
            System.out.println("Notify " + u.getUsername() + " : " + message);
        }
    }

    @Override
    public String toString() {
        return String.format("Event{id=%d, name=%s, subs=%d}", id, name, subscribers.size());
    }

}
