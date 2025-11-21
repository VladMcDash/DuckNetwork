package ducknetwork.repository;

import ducknetwork.exceptions.DomainExceptions;
import ducknetwork.persistence.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EventSubscriberRepository {

    private final EventRepository eventRepo = new EventRepository();
    private final UserRepository userRepo = new UserRepository();

    public void subscribe(Long eventId, Long userId) {
        if (eventRepo.findById(eventId).isEmpty()) {
            throw new RuntimeException("Event not found");
        }
        if (!userRepo.existsById(userId)) {
            throw new DomainExceptions.UserNotFoundException("User not found");
        }

        String sql = "INSERT INTO event_subscribers (event_id, user_id) VALUES (?, ?) ON CONFLICT (event_id, user_id) DO NOTHING";

        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, eventId);
            ps.setLong(2, userId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to subscribe user " + userId + " to event " + eventId + ": " + e.getMessage(), e);
        }
    }

    public void unsubscribe(Long eventId, Long userId) {
        String sql = "DELETE FROM event_subscribers WHERE event_id = ? AND user_id = ?";

        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, eventId);
            ps.setLong(2, userId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to unsubscribe user " + userId + " from event " + eventId + ": " + e.getMessage(), e);
        }
    }

    public List<Long> findSubscribers(Long eventId) {
        List<Long> list = new ArrayList<>();

        String sql = "SELECT user_id FROM event_subscribers WHERE event_id = ?";

        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getLong("user_id"));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load subscribers for event " + eventId + ": " + e.getMessage(), e);
        }

        return list;
    }
}