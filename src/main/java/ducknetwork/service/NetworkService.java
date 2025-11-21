package ducknetwork.service;

import ducknetwork.domain.*;
import ducknetwork.repository.Repo;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service layer
 */
public class NetworkService {

    private final Repo repo = Repo.getInstance();


    public void addFriend(Long id1, Long id2) {
        if (id1 == null || id2 == null) throw new IllegalArgumentException("IDs cannot be null");
        if (id1.equals(id2)) throw new IllegalArgumentException("A user cannot befriend themselves");
        repo.addFriend(id1, id2);
    }

    public void removeFriend(Long id1, Long id2) {
        repo.removeFriend(id1, id2);
    }

    public int numberOfCommunities() {
        return getCommunities().size();
    }

    /**
     * Compute connected components of the friendship graph.
     */
    public List<List<User>> getCommunities() {
        List<User> allUsers = repo.listAllUsers();
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

            // Read friends from DB
            for (Long fid : repo.getFriendIds(cur)) {
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

            for (Long fid : repo.getFriendIds(cur)) {
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
        return repo.createCard(name.trim());
    }

    public void removeCard(Long cardId) {
        repo.removeCard(cardId);
    }

    public List<Card> listCards() {
        return repo.listCards();
    }

    public void addDuckToCard(Long duckId, Long cardId) {
        User u = repo.findById(duckId);
        if (!(u instanceof Duck))
            throw new IllegalArgumentException("Only ducks can join cards");
        repo.addDuckToCard(duckId, cardId);
    }

    public void removeDuckFromCard(Long duckId, Long cardId) {
        repo.removeDuckFromCard(duckId, cardId);
    }

    public List<Duck> getCardMembers(Long cardId) {
        Card c = repo.findCard(cardId);
        if (c == null) return List.of();

        List<Duck> allDucks = repo.listAllDucks();

        return allDucks.stream()
                .filter(d -> c.hasDuck(d))
                .collect(Collectors.toList());
    }


    public Event createEvent(String name) {
        return repo.createEvent(name);
    }

    public RaceEvent createRaceEvent(String name, List<Double> buoys) {
        return repo.createRaceEvent(name, buoys);
    }

    public void subscribeToEvent(Long eventId, Long userId) {
        repo.subscribeToEvent(eventId, userId);
    }

    public void unsubscribeFromEvent(Long eventId, Long userId) {
        repo.unsubscribeFromEvent(eventId, userId);
    }

    public Map<Duck, Double> runRace(Long eventId, int M) {
        Event e = repo.findEvent(eventId);
        if (!(e instanceof RaceEvent re))
            throw new RuntimeException("Not a race event!");

        List<Duck> pool = repo.listAllDucks();
        re.autoSelectParticipants(pool, M);
        return re.simulateRace();
    }
}
