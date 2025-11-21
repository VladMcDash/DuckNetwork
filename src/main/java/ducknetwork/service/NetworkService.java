package ducknetwork.service;

import ducknetwork.domain.*;
import ducknetwork.exceptions.DomainExceptions;
import ducknetwork.repository.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service layer. Utilizează Repository-uri dedicate pentru persistență.
 */
public class NetworkService {

    // Dependințe injectate (instanțiate aici pentru simplitate)
    private final UserRepository userRepo = new UserRepository();
    private final FriendRepository friendRepo = new FriendRepository();
    private final CardRepository cardRepo = new CardRepository();
    private final CardMemberRepository cardMemberRepo = new CardMemberRepository();
    private final EventRepository eventRepo = new EventRepository();
    private final EventSubscriberRepository eventSubscriberRepo = new EventSubscriberRepository();
    private final DuckRepository duckRepo = new DuckRepository();


    /**
     * Adaugă un utilizator (Person sau Duck) prin UserRepository.
     */
    public User addUser(User user) {
        return userRepo.save(user);
    }

    /**
     * Șterge un utilizator prin UserRepository.
     */
    public void removeUser(Long id) {
        userRepo.delete(id);
    }

    /**
     * Gasește un utilizator după ID.
     */
    public User findById(Long id) {
        User u = userRepo.findById(id);
        if (u == null)
            throw new DomainExceptions.UserNotFoundException("User with id " + id + " not found");
        return u;
    }

    /**
     * Listeaza toți utilizatorii (Persoane + Rate).
     */
    public List<User> listAllUsers() {
        return userRepo.findAll();
    }

    public void addFriend(Long id1, Long id2) {
        if (id1 == null || id2 == null) throw new IllegalArgumentException("IDs cannot be null");
        if (id1.equals(id2)) throw new IllegalArgumentException("A user cannot befriend themselves");
        friendRepo.addFriend(id1, id2);
    }

    public void removeFriend(Long id1, Long id2) {
        friendRepo.removeFriend(id1, id2);
    }

    public int numberOfCommunities() {
        return getCommunities().size();
    }

    /**
     * Compute connected components of the friendship graph.
     */
    public List<List<User>> getCommunities() {
        List<User> allUsers = userRepo.findAll();
        Map<Long, User> byId = allUsers.stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        Set<Long> visited = new HashSet<>();
        List<List<User>> components = new ArrayList<>();

        for (User u : allUsers) {
            long uid = u.getId();
            if (!visited.contains(uid)) {
                components.add(bfsCollectComponent(uid, visited, byId));
            }
        }

        return components;
    }

    private List<User> bfsCollectComponent(Long startId,
                                           Set<Long> visited,
                                           Map<Long, User> byId) {

        List<User> component = new ArrayList<>();
        Queue<Long> q = new LinkedList<>();

        visited.add(startId);
        q.add(startId);

        while (!q.isEmpty()) {
            Long cur = q.poll();
            User u = byId.get(cur);
            if (u == null) continue;

            component.add(u);

            for (Long fid : friendRepo.getFriendIds(cur)) {
                if (!visited.contains(fid)) {
                    visited.add(fid);
                    q.add(fid);
                }
            }
        }

        return component;
    }

    public List<User> mostSociableCommunity() {
        List<List<User>> comps = getCommunities();

        List<User> best = Collections.emptyList();
        int bestDiam = -1;

        for (List<User> comp : comps) {
            int diam = diameterOfComponent(comp);
            if (diam > bestDiam) {
                bestDiam = diam;
                best = comp;
            }
        }

        return best;
    }

    private int diameterOfComponent(List<User> comp) {
        Set<Long> allowed = comp.stream()
                .map(User::getId)
                .collect(Collectors.toSet());

        int max = 0;
        for (User u : comp) {
            max = Math.max(max, bfsMaxDistance(u.getId(), allowed));
        }
        return max;
    }

    private int bfsMaxDistance(Long startId, Set<Long> allowedIds) {
        Queue<Long> q = new LinkedList<>();
        Map<Long, Integer> dist = new HashMap<>();

        q.add(startId);
        dist.put(startId, 0);

        int max = 0;

        while (!q.isEmpty()) {
            Long cur = q.poll();
            int d = dist.get(cur);

            for (Long fid : friendRepo.getFriendIds(cur)) {
                if (!allowedIds.contains(fid)) continue;

                if (!dist.containsKey(fid)) {
                    dist.put(fid, d + 1);
                    max = Math.max(max, d + 1);
                    q.add(fid);
                }
            }
        }

        return max;
    }

    public Card createCard(String name) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Card name required");
        return cardRepo.save(name.trim());
    }

    public void removeCard(Long cardId) {
        cardRepo.delete(cardId);
    }

    public List<Card> listCards() {
        return cardRepo.findAll();
    }

    public void addDuckToCard(Long duckId, Long cardId) {
        // Folosim findById pentru verificare
        User u = userRepo.findById(duckId);
        if (u == null)
            throw new DomainExceptions.UserNotFoundException("User with id " + duckId + " not found");

        if (!(u instanceof Duck))
            throw new IllegalArgumentException("Only ducks can join cards");

        cardMemberRepo.addDuckToCard(duckId, cardId);
    }

    public void removeDuckFromCard(Long duckId, Long cardId) {
        cardMemberRepo.removeDuckFromCard(duckId, cardId);
    }

    public List<Duck> getCardMembers(Long cardId) {
        if (cardRepo.findById(cardId).isEmpty()) {
            return List.of();
        }

        List<Long> duckIds = cardMemberRepo.findDucksInCard(cardId);

        return duckIds.stream()
                .map(duckRepo::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }


    public Event createEvent(String name) {
        return eventRepo.save(name);
    }

    public RaceEvent createRaceEvent(String name, List<Double> buoys) {
        return eventRepo.saveRaceEvent(name, buoys);
    }

    /**
     * Listează toate evenimentele prin EventRepository.
     */
    public List<Event> listEvents() {
        return eventRepo.findAll();
    }


    public void subscribeToEvent(Long eventId, Long userId) {
        eventSubscriberRepo.subscribe(eventId, userId);
    }

    public void unsubscribeFromEvent(Long eventId, Long userId) {
        eventSubscriberRepo.unsubscribe(eventId, userId);
    }

    public Map<Duck, Double> runRace(Long eventId, int M) {
        Event e = eventRepo.findById(eventId).orElse(null);

        if (e == null)
            throw new RuntimeException("Event with id " + eventId + " not found!");

        if (!(e instanceof RaceEvent re))
            throw new RuntimeException("Not a race event!");

        List<Duck> pool = duckRepo.findAll();
        re.autoSelectParticipants(pool, M);
        return re.simulateRace();
    }
}