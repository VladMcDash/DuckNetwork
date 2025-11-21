package ducknetwork.persistence;

import ducknetwork.domain.Event;
import ducknetwork.domain.RaceEvent;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventDao {

    /**
     * Creează un eveniment normal ("EVENT")
     */
    public long insertEvent(String name) {
        String sql = """
                INSERT INTO events (name, type)
                VALUES (?, 'EVENT')
                RETURNING id
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getLong("id");

        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert event", e);
        }

        return -1;
    }

    /**
     * Creează un eveniment de tip RACE
     */
    public long insertRaceEvent(String name) {
        String sql = """
                INSERT INTO events (name, type)
                VALUES (?, 'RACE')
                RETURNING id
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getLong("id");

        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert race event", e);
        }

        return -1;
    }
    private List<Double> loadBuoys(Long eventId) {
        List<Double> list = new ArrayList<>();

        String sql = "SELECT distance FROM race_buoys WHERE event_id = ? ORDER BY position";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, eventId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getDouble("distance"));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error loading buoys", e);
        }

        return list;
    }


    /**
     * Returnează Event sau RaceEvent, în funcție de coloana type
     */
    public List<Event> findAll() {
        List<Event> list = new ArrayList<>();

        String sql = "SELECT id, name, is_race FROM events ORDER BY id";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                long id = rs.getLong("id");
                String name = rs.getString("name");
                boolean isRace = rs.getBoolean("is_race");

                if (!isRace) {
                    list.add(new Event(id, name));
                } else {
                    List<Double> buoys = loadBuoys(id);
                    list.add(new RaceEvent(id, name, buoys)); // ← FIX
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error reading events", e);
        }

        return list;
    }


    public void delete(long id) {
        String sql = "DELETE FROM events WHERE id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete event " + id, e);
        }
    }
}
