package ducknetwork.repository;

import ducknetwork.domain.*;
import ducknetwork.exceptions.DomainExceptions;
import ducknetwork.util.Validators;
import ducknetwork.persistence.PersonDao;
import ducknetwork.persistence.DuckDao;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Repository layer for DuckSocialNetwork : users, cards, events storage & CRUD.
 * Now supports JDBC persistence for Person and Duck.
 */
public class Repo {

    private static Repo INSTANCE;
    private final Map<Long, User> users = new HashMap<>();
    private final Map<Long, Card> cards = new HashMap<>();
    private final Map<Long, Event> events = new HashMap<>();

    private final AtomicLong userIdGen = new AtomicLong(1);
    private final AtomicLong cardIdGen = new AtomicLong(1);
    private final AtomicLong eventIdGen = new AtomicLong(1);

    /** NEW: JDBC DAOs */
    private final PersonDao personDao = new PersonDao();
    private final DuckDao duckDao = new DuckDao();

    private Repo() {}

    public static synchronized Repo getInstance() {
        if (INSTANCE == null) INSTANCE = new Repo();
        return INSTANCE;
    }
    public synchronized void loadFromDatabase() {

        for (Person p : personDao.findAll()) {
            userIdGen.updateAndGet(curr -> Math.max(curr, p.getId() + 1));
            users.put(p.getId(), p);
        }

        for (Duck d : duckDao.findAll()) {
            userIdGen.updateAndGet(curr -> Math.max(curr, d.getId() + 1));
            users.put(d.getId(), d);
        }
    }
   public synchronized User addUser(User user) {
        if (user == null) throw new IllegalArgumentException("User cannot be null");
        Validators.validate(Validators.USERNAME_VALIDATOR, user.getUsername());
        Validators.validate(Validators.EMAIL_VALIDATOR, user.getEmail());

        if (user.getId() == null || users.containsKey(user.getId())) {
            long newId = userIdGen.getAndIncrement();
            user.setId(newId);
        } else {
            userIdGen.updateAndGet(curr -> Math.max(curr, user.getId() + 1));
        }

        users.put(user.getId(), user);

        if (user instanceof Person p) {
            personDao.save(p);
        } else if (user instanceof Duck d) {
            duckDao.save(d);
        }

        return user;
    }

    public synchronized void removeUser(Long id) {
        User removed = users.remove(id);

        if (removed == null)
            throw new DomainExceptions.UserNotFoundException("User with id " + id + " not found");

        if (removed instanceof Person)
            personDao.delete(id);
        else if (removed instanceof Duck)
            duckDao.delete(id);
        for (User u : users.values()) u.removeFriend(removed);

        for (Card c : cards.values()) {
            if (removed instanceof Duck) {
                c.removeDuck((Duck) removed);
            }
        }

        for (Event e : events.values()) e.unsubscribe(removed);
    }


    public synchronized User findById(Long id) {
        User u = users.get(id);
        if (u == null) throw new DomainExceptions.UserNotFoundException("User with id " + id + " not found");
        return u;
    }

    public synchronized List<User> listAllUsers() {
        return users.values().stream().sorted(Comparator.comparing(User::getId)).collect(Collectors.toList());
    }

    public synchronized List<Duck> listAllDucks() {
        return users.values().stream().filter(u -> u instanceof Duck).map(u -> (Duck) u).collect(Collectors.toList());
    }

    public synchronized void addFriend(Long id1, Long id2) {
        User a = users.get(id1);
        User b = users.get(id2);
        if (a == null || b == null) throw new DomainExceptions.UserNotFoundException("One or both users not found");
        a.addFriend(b);
        b.addFriend(a);
    }

    public synchronized void removeFriend(Long id1, Long id2) {
        User a = users.get(id1);
        User b = users.get(id2);
        if (a == null || b == null) throw new DomainExceptions.UserNotFoundException("One or both users not found");
        a.removeFriend(b);
        b.removeFriend(a);
    }

    public synchronized Card createCard(String name) {
        long id = cardIdGen.getAndIncrement();
        Card c = new Card(id, name);
        cards.put(id, c);
        return c;
    }

    public synchronized void removeCard(Long cardId) {
        Card c = cards.remove(cardId);
        if (c == null) throw new RuntimeException("Card not found");
        for (Duck d : new ArrayList<>(c.getMembers())) c.removeDuck(d);
    }

    public synchronized Card findCard(Long cardId) {
        return cards.get(cardId);
    }

    public synchronized List<Card> listCards() {
        return new ArrayList<>(cards.values());
    }

    public synchronized void addDuckToCard(Long duckId, Long cardId) {
        User u = users.get(duckId);
        Card c = cards.get(cardId);
        if (u == null) throw new DomainExceptions.UserNotFoundException("Duck not found");
        if (c == null) throw new RuntimeException("Card not found");
        if (!(u instanceof Duck)) throw new RuntimeException("User is not a Duck");
        c.addDuck((Duck)u);
    }

    public synchronized void removeDuckFromCard(Long duckId, Long cardId) {
        User u = users.get(duckId);
        Card c = cards.get(cardId);
        if (u == null) throw new DomainExceptions.UserNotFoundException("Duck not found");
        if (c == null) throw new RuntimeException("Card not found");
        if (!(u instanceof Duck)) throw new RuntimeException("User is not a Duck");
        c.removeDuck((Duck) u);
    }

    public synchronized Event createEvent(String name) {
        long id = eventIdGen.getAndIncrement();
        Event e = new Event(id, name);
        events.put(id, e);
        return e;
    }

    public synchronized RaceEvent createRaceEvent(String name, List<Double> buoys) {
        long id = eventIdGen.getAndIncrement();
        RaceEvent re = new RaceEvent(id, name, buoys);
        events.put(id, re);
        return re;
    }

    public synchronized void removeEvent(Long eventId) {
        events.remove(eventId);
    }

    public synchronized Event findEvent(Long eventId) {
        return events.get(eventId);
    }

    public synchronized List<Event> listEvents() {
        return new ArrayList<>(events.values());
    }

    public synchronized void clearAll() {
        users.clear(); cards.clear(); events.clear();
    }
}
