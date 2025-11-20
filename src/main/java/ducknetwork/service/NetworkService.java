package ducknetwork.service;

import ducknetwork.domain.*;
import ducknetwork.exceptions.DomainExceptions;
import ducknetwork.repository.Repo;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service layer for DuckSocialNetwork.
 * Business logic: communities, most sociable, plus card & event orchestration.
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

    public List<List<User>> getCommunities() {
        List<User> all = repo.listAllUsers();
        Map<Long, User> map = all.stream().collect(Collectors.toMap(User::getId, u -> u));
        Set<Long> visited = new HashSet<>();
        List<List<User>> comps = new ArrayList<>();
        for (User u : all) {
            if (!visited.contains(u.getId())) {
                List<User> comp = new ArrayList<>();
                Queue<Long> q = new LinkedList<>();
                q.add(u.getId()); visited.add(u.getId());
                while (!q.isEmpty()) {
                    Long cur = q.poll();
                    User usr = map.get(cur);
                    if (usr == null) continue;
                    comp.add(usr);
                    for (User f : usr.getFriends()) {
                        if (!visited.contains(f.getId())) {
                            visited.add(f.getId());
                            q.add(f.getId());
                        }
                    }
                }
                comps.add(comp);
            }
        }
        return comps;
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
        Set<Long> allowed = comp.stream().map(User::getId).collect(Collectors.toSet());
        int diam = 0;
        for (User u : comp) diam = Math.max(diam, bfsMaxDistance(u.getId(), allowed));
        return diam;
    }

    private int bfsMaxDistance(Long startId, Set<Long> allowed) {
        List<User> all = repo.listAllUsers();
        Map<Long, User> map = all.stream().collect(Collectors.toMap(User::getId, u -> u));
        Queue<Long> q = new LinkedList<>();
        Map<Long,Integer> dist = new HashMap<>();
        q.add(startId); dist.put(startId, 0);
        int max = 0;
        while (!q.isEmpty()) {
            Long cur = q.poll();
            User u = map.get(cur);
            if (u == null) continue;
            int cd = dist.get(cur);
            for (User f : u.getFriends()) {
                if (!allowed.contains(f.getId())) continue;
                if (!dist.containsKey(f.getId())) {
                    dist.put(f.getId(), cd + 1);
                    max = Math.max(max, cd + 1);
                    q.add(f.getId());
                }
            }
        }
        return max;
    }


    public Card createCard(String name) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Card name required");
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
        if (!(u instanceof Duck)) throw new IllegalArgumentException("User is not a Duck");
        repo.addDuckToCard(duckId, cardId);
    }

    public void removeDuckFromCard(Long duckId, Long cardId) {
        repo.removeDuckFromCard(duckId, cardId);
    }

    public Event createEvent(String name) {
        return repo.createEvent(name);
    }

    public RaceEvent createRaceEvent(String name, List<Double> buoys) {
        return repo.createRaceEvent(name, buoys);
    }

    public void subscribeToEvent(Long eventId, Long userId) {
        Event e = repo.findEvent(eventId);
        User u = repo.findById(userId);
        if (e == null) throw new RuntimeException("Event not found");
        e.subscribe(u);
    }

    public void unsubscribeFromEvent(Long eventId, Long userId) {
        Event e = repo.findEvent(eventId);
        User u = repo.findById(userId);
        if (e == null) throw new RuntimeException("Event not found");
        e.unsubscribe(u);
    }

    /**
     * Auto-select M swimmers and run race. Returns map duck -> time.
     */
    public Map<Duck, Double> runRace(Long eventId, int M) {
        Event e = repo.findEvent(eventId);
        if (!(e instanceof RaceEvent)) throw new RuntimeException("Event is not a RaceEvent");
        RaceEvent re = (RaceEvent) e;
        List<Duck> pool = repo.listAllDucks();
        re.autoSelectParticipants(pool, M);
        return re.simulateRace();
    }
}
