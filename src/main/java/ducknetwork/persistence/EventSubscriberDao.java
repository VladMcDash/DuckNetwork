package ducknetwork.persistence;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventSubscriberDao {

    public void subscribe(long eventId, long userId) {
        String sql = """
                INSERT INTO event_subscribers (event_id, user_id)
                VALUES (?, ?)
                ON CONFLICT DO NOTHING
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, eventId);
            ps.setLong(2, userId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to subscribe user", e);
        }
    }

    public void unsubscribe(long eventId, long userId) {
        String sql = "DELETE FROM event_subscribers WHERE event_id = ? AND user_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, eventId);
            ps.setLong(2, userId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to unsubscribe user", e);
        }
    }

    public List<Long> findSubscribers(long eventId) {
        List<Long> list = new ArrayList<>();

        String sql = "SELECT user_id FROM event_subscribers WHERE event_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, eventId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(rs.getLong("user_id"));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load subscribers", e);
        }

        return list;
    }
}
