package ducknetwork.repository;

import ducknetwork.domain.*;
import ducknetwork.exceptions.DomainExceptions;
import ducknetwork.util.Validators;
import ducknetwork.persistence.*;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;

/**
 * Repo in Database
 */
public class Repo {

    private static Repo INSTANCE;

    private final UserDao userDao = new UserDao();
    private final PersonDao personDao = new PersonDao();
    private final DuckDao duckDao = new DuckDao();
    private final FriendsDao friendsDao = new FriendsDao();
    private final CardDao cardDao = new CardDao();
    private final CardMembersDao cardMembershipDao = new CardMembersDao();
    private final EventDao eventDao = new EventDao();
    private final EventSubscriberDao eventSubscriberDao = new EventSubscriberDao();
    private final RaceBuoyDao raceBuoyDao = new RaceBuoyDao();

    private Repo() {}

    public static synchronized Repo getInstance() {
        if (INSTANCE == null) INSTANCE = new Repo();
        return INSTANCE;
    }

    /**
     * Add a generic user (Person or Duck). Validates username/email.
     * Persists to DB via PersonDao or DuckDao and returns the persisted object (with ID).
     */
    public synchronized User addUser(User user) {
        if (user == null)
            throw new IllegalArgumentException("User cannot be null");

        Validators.validate(Validators.USERNAME_VALIDATOR, user.getUsername());
        Validators.validate(Validators.EMAIL_VALIDATOR, user.getEmail());

        try {
            String sql = "INSERT INTO users(username, email, password, type) VALUES (?, ?, ?, ?) RETURNING id";
            try (Connection conn = Database.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, user.getUsername());
                ps.setString(2, user.getEmail());
                ps.setString(3, user.getPassword());
                ps.setString(4, (user instanceof Duck) ? "DUCK" : "PERSON");

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        long newId = rs.getLong("id");
                        user.setId(newId);
                    } else {
                        throw new RuntimeException("Failed to retrieve generated user id");
                    }
                }
            }


            if (user instanceof Person p) {
                return personDao.save(p);
            } else if (user instanceof Duck d) {
                return duckDao.save(d);
            } else {
                throw new IllegalArgumentException("Unsupported User subtype: " + user.getClass());
            }

        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new RuntimeException("Duplicate username or email", e);
            }
            throw new RuntimeException("Error adding user", e);
        }
    }




    /**
     * Remove user by id from DB. Cascade rules in DB will remove detail rows (person/duck),
     * friends entries, card memberships and event subscribers if schema uses ON DELETE CASCADE.
     */
    public synchronized void removeUser(Long id) {
        // Check existence
        Optional<Person> p = personDao.findById(id);
        Optional<Duck> d = duckDao.findById(id);
        if (p.isEmpty() && d.isEmpty()) {
            throw new DomainExceptions.UserNotFoundException("User with id " + id + " not found");
        }

        userDao.delete(id);
    }

    /**
     * Find user by id: first try Person, then Duck.
     */
    public synchronized User findById(Long id) {
        Optional<Person> p = personDao.findById(id);
        if (p.isPresent()) return p.get();
        Optional<Duck> d = duckDao.findById(id);
        if (d.isPresent()) return d.get();
        throw new DomainExceptions.UserNotFoundException("User with id " + id + " not found");
    }

    /**
     * List all users (Persons + Ducks) combined, ordered by id.
     */
    public synchronized List<User> listAllUsers() {
        List<User> users = new ArrayList<>();
        users.addAll(personDao.findAll());
        users.addAll(duckDao.findAll());
        return users.stream()
                .sorted(Comparator.comparing(User::getId))
                .collect(Collectors.toList());
    }

    /**
     * List all ducks.
     */
    public synchronized List<Duck> listAllDucks() {
        return duckDao.findAll();
    }

    /**
     * Make two users friends (symmetric). We insert both directions to keep simple symmetric relation.
     */
    public synchronized void addFriend(Long id1, Long id2) {
        if (Objects.equals(id1, id2)) throw new IllegalArgumentException("A user cannot befriend themselves");
        ensureUserExists(id1);
        ensureUserExists(id2);


        friendsDao.addFriend(id1, id2);
        friendsDao.addFriend(id2, id1);
    }

    /**
     * Remove friendship bilateral.
     */
    public synchronized void removeFriend(Long id1, Long id2) {
        ensureUserExists(id1);
        ensureUserExists(id2);

        friendsDao.removeFriend(id1, id2);
        friendsDao.removeFriend(id2, id1);
    }

    /**
     * Return friend ids for a user.
     */
    public synchronized List<Long> getFriendIds(Long userId) {
        ensureUserExists(userId);
        return friendsDao.getFriendsOf(userId);
    }

    public synchronized Card createCard(String name) {
        long id = cardDao.insertCard(name);
        return new Card(id, name);
    }
    public synchronized void removeCard(Long cardId) {
        // existence check optional
        cardDao.deleteCard(cardId);
    }

    public synchronized Card findCard(Long cardId) {
        return cardDao.findAll().stream()
                .filter(c -> Objects.equals(c.getId(), cardId))
                .findFirst()
                .orElse(null);
    }

    public synchronized List<Card> listCards() {
        return cardDao.findAll();
    }

    /**
     * Add duck to card (persist membership).
     */
    public synchronized void addDuckToCard(Long duckId, Long cardId) {
        //exista rata
        if (duckDao.findById(duckId).isEmpty()) throw new DomainExceptions.UserNotFoundException("Duck not found");
        //exista card
        Card card = findCard(cardId);
        if (card == null) throw new RuntimeException("Card not found");

        cardMembershipDao.addDuckToCard(duckId, cardId);
    }

    public synchronized void removeDuckFromCard(Long duckId, Long cardId) {
        if (duckDao.findById(duckId).isEmpty()) throw new DomainExceptions.UserNotFoundException("Duck not found");
        cardMembershipDao.removeDuckFromCard(duckId, cardId);
    }

    public synchronized Event createEvent(String name) {
        long id = eventDao.insertEvent(name);
        return new Event(id, name);
    }

    public synchronized RaceEvent createRaceEvent(String name, List<Double> buoys) {
        long id = eventDao.insertRaceEvent(name);
        // insert buoys
        int pos = 1;
        for (Double d : buoys) {
            raceBuoyDao.insertBuoy(id, d, pos++);
        }
        return new RaceEvent(id, name, buoys);
    }

    public synchronized void removeEvent(Long eventId) {
        eventDao.delete(eventId);
    }

    public synchronized Event findEvent(Long eventId) {
        return eventDao.findAll().stream()
                .filter(e -> Objects.equals(e.getId(), eventId))
                .findFirst()
                .orElse(null);
    }

    public synchronized List<Event> listEvents() {
        return eventDao.findAll();
    }

    /**
     * Subscribe a user to an event.
     */
    public synchronized void subscribeToEvent(Long eventId, Long userId) {

        if (eventDao.findAll().stream().noneMatch(e -> Objects.equals(e.getId(), eventId)))
            throw new RuntimeException("Event not found");
        if (userDao.findAllBaseUsers().stream().noneMatch(b -> b.id() == userId))
            throw new DomainExceptions.UserNotFoundException("User not found");

        eventSubscriberDao.subscribe(eventId, userId);
    }

    public synchronized void unsubscribeFromEvent(Long eventId, Long userId) {
        eventSubscriberDao.unsubscribe(eventId, userId);
    }

    public synchronized List<Long> listSubscribers(Long eventId) {
        return eventSubscriberDao.findSubscribers(eventId);
    }

    private void ensureUserExists(Long id) {
        boolean exists = userDao.findAllBaseUsers().stream().anyMatch(b -> b.id() == id);
        if (!exists) throw new DomainExceptions.UserNotFoundException("User with id " + id + " not found");
    }

    public synchronized void clearAll() {
        throw new UnsupportedOperationException("clearAll() is not supported in FULL DB mode. Use DB admin tools or a migration script to reset the database.");
    }
}
