package ducknetwork.repository;

import ducknetwork.domain.Event;
import ducknetwork.domain.RaceEvent;
import ducknetwork.persistence.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EventRepository {

    private final RaceBuoyRepository raceBuoyRepo = new RaceBuoyRepository();

    // Nu mai exista dependentele UserRepository/EventSubscriberRepository in aceasta logica de incarcare

    public Event save(String name) {
        String sql = "INSERT INTO events (name, type) VALUES (?, 'EVENT') RETURNING id";

        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long id = rs.getLong("id");
                    return new Event(id, name);
                } else {
                    throw new RuntimeException("Failed to retrieve generated event id");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert generic event: " + e.getMessage(), e);
        }
    }

    public RaceEvent saveRaceEvent(String name, List<Double> buoys) {
        String eventSql = "INSERT INTO events (name, type) VALUES (?, 'RACE') RETURNING id";
        Connection conn = null;

        try {
            conn = Database.getInstance().getConnection();
            conn.setAutoCommit(false);

            long id;
            try (PreparedStatement ps = conn.prepareStatement(eventSql)) {
                ps.setString(1, name);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        id = rs.getLong("id");
                    } else {
                        conn.rollback();
                        throw new RuntimeException("Failed to retrieve generated race event id");
                    }
                }
            }

            raceBuoyRepo.saveBuoys(conn, id, buoys);

            conn.commit();
            return new RaceEvent(id, name, buoys);

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignore) {}
            }
            throw new RuntimeException("Failed to create race event: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignore) {}
            }
        }
    }

    public void delete(Long eventId) {
        String sql = "DELETE FROM events WHERE id = ?";

        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, eventId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete event " + eventId + ": " + e.getMessage(), e);
        }
    }

    /**
     * ATENTIE: Aceasta metoda nu mai populeaza lista de subscribers din obiectul Event.
     */
    public Optional<Event> findById(Long eventId) {
        String sql = "SELECT id, name, type FROM events WHERE id = ?";

        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long id = rs.getLong("id");
                    String name = rs.getString("name");
                    String type = rs.getString("type");

                    if ("RACE".equals(type)) {
                        // findBuoys arunca SQLException, trebuie tratata local
                        List<Double> buoys = raceBuoyRepo.findBuoys(conn, id);
                        return Optional.of(new RaceEvent(id, name, buoys));
                    } else {
                        return Optional.of(new Event(id, name));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find event " + eventId + ": " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    /**
     * ATENTIE: Aceasta metoda nu mai populeaza lista de subscribers din obiectul Event.
     */
    public List<Event> findAll() {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT id, name, type FROM events ORDER BY id";

        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                long id = rs.getLong("id");
                String name = rs.getString("name");
                String type = rs.getString("type");

                if ("RACE".equals(type)) {
                    // findBuoys arunca SQLException, trebuie tratata local
                    List<Double> buoys = raceBuoyRepo.findBuoys(conn, id);
                    events.add(new RaceEvent(id, name, buoys));
                } else {
                    events.add(new Event(id, name));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load events: " + e.getMessage(), e);
        }
        return events;
    }
}