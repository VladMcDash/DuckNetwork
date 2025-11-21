package ducknetwork.repository;

import ducknetwork.exceptions.DomainExceptions;
import ducknetwork.persistence.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Handles persistence operations for the Friends relationship, using the constraint user_id < friend_id.
 */
public class FriendRepository {

    private final UserRepository userRepo = new UserRepository();

    /**
     * Make two users friends (stored only once: minId -> maxId).
     */
    public void addFriend(Long id1, Long id2) {
        if (Objects.equals(id1, id2)) throw new IllegalArgumentException("A user cannot befriend themselves");
        ensureUserExists(id1);
        ensureUserExists(id2);

        Long minId = Math.min(id1, id2);
        Long maxId = Math.max(id1, id2);

        insertFriendship(minId, maxId);
    }

    private void insertFriendship(Long minId, Long maxId) {
        String sql = "INSERT INTO friends (user_id, friend_id) VALUES (?, ?) ON CONFLICT (user_id, friend_id) DO NOTHING";

        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, minId);
            ps.setLong(2, maxId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to add friend: " + minId + " -> " + maxId, e);
        }
    }

    /**
     * Remove friendship bilateral (ștergem o singură înregistrare: minId -> maxId).
     */
    public void removeFriend(Long id1, Long id2) {
        ensureUserExists(id1);
        ensureUserExists(id2);

        Long minId = Math.min(id1, id2);
        Long maxId = Math.max(id1, id2);

        deleteFriendship(minId, maxId);
    }

    private void deleteFriendship(Long minId, Long maxId) {
        String sql = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";

        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, minId);
            ps.setLong(2, maxId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to remove friend: " + minId + " -> " + maxId, e);
        }
    }

    /**
     * Return friend ids for a user, căutând în ambele coloane (user_id OR friend_id).
     */
    public List<Long> getFriendIds(Long userId) {
        ensureUserExists(userId);

        List<Long> friendIds = new ArrayList<>();

        String sql = """
            SELECT 
                CASE
                    WHEN user_id = ? THEN friend_id 
                    ELSE user_id
                END AS friend_id 
            FROM friends 
            WHERE user_id = ? OR friend_id = ?
            """;

        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            ps.setLong(2, userId);
            ps.setLong(3, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    friendIds.add(rs.getLong("friend_id"));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to get friends for user " + userId, e);
        }

        return friendIds;
    }

    private void ensureUserExists(Long id) {
        if (!userRepo.existsById(id)) {
            throw new DomainExceptions.UserNotFoundException("User with id " + id + " not found");
        }
    }
}