package ducknetwork.domain;

import java.util.*;
import java.util.stream.Collectors;

/**
 * RaceEvent specialized for swimming ducks.
 * It holds buoys distances. It can auto-select M participants according to endurance ordering,
 * and compute times t_i = max_j (2 * d_j / v_i). Total race time is max over participants.
 */
public class RaceEvent extends Event {
    private final List<Double> buoys = new ArrayList<>(); // distances d_j
    private final List<Duck> participants = new ArrayList<>();

    public RaceEvent(Long id, String name, List<Double> buoys) {
        super(id, name);
        if (buoys != null) this.buoys.addAll(buoys);
    }

    /**
     * Select M swimmers from given pool according to endurance descending (r1 >= r2 >= ...).
     */
    public void autoSelectParticipants(List<Duck> pool, int M) {
        if (pool == null) pool = Collections.emptyList();
        List<Duck> swimmers = pool.stream()
                .filter(d -> d instanceof Inotator)
                .sorted(Comparator.comparingDouble(Duck::getEndurance).reversed()
                        .thenComparing(Comparator.comparingDouble(Duck::getSpeed).reversed()))
                .limit(M)
                .toList();
        participants.clear();
        participants.addAll(swimmers);
    }

    /**
     * Simulate race: returns a map Duck -> time (t_i), and prints summary.
     */
    public Map<Duck, Double> simulateRace() {
        Map<Duck, Double> times = new LinkedHashMap<>();
        for (Duck d : participants) {
            double maxT = 0.0;
            for (double dist : buoys) {
                double t = (2.0 * dist) / d.getSpeed();
                if (t > maxT) maxT = t;
            }
            times.put(d, maxT);
        }
        double total = times.values().stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        notifySubscribers("Race " + name + " finished in " + String.format("%.3f", total) + "s");
        int lane = 1;
        for (Map.Entry<Duck, Double> e : times.entrySet()) {
            System.out.printf("Duck %s on lane %d: t = %.3f s%n", e.getKey().getUsername(), lane++, e.getValue());
        }
        return times;
    }

    public List<Duck> getParticipants() { return participants; }
}
