package ducknetwork.persistence;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FriendsDao {

    public void addFriend(long userId, long friendId) {

        String sql = """
            INSERT INTO friends (user_id, friend_id)
            VALUES (?, ?)
            ON CONFLICT DO NOTHING
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            ps.setLong(2, friendId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to add friend", e);
        }
    }

    public void removeFriend(long userId, long friendId) {

        String sql = """
            DELETE FROM friends WHERE user_id = ? AND friend_id = ?
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            ps.setLong(2, friendId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to remove friend", e);
        }
    }

    public List<Long> findFriends(long userId) {

        List<Long> list = new ArrayList<>();

        String sql = """
            SELECT friend_id FROM friends WHERE user_id = ?
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(rs.getLong("friend_id"));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load friends", e);
        }

        return list;
    }
    public List<Long> getFriendsOf(Long userId) {
        String sql = "SELECT friend_id FROM friends WHERE user_id = ?";
        List<Long> result = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(rs.getLong("friend_id"));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error loading friends", e);
        }

        return result;
    }

}
