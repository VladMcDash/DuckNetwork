package ducknetwork.persistence;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDao {

    public record BaseUser(
            long id,
            String username,
            String email,
            String password,
            String type
    ) {}

    public long insertUser(String username, String email, String password, String type) {
        String sql = """
                INSERT INTO users (username, email, password, type)
                VALUES (?, ?, ?, ?)
                RETURNING id
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, password);
            ps.setString(4, type);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getLong("id");

        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert user", e);
        }
        return -1;
    }

    public void delete(long id) {
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete user " + id, e);
        }
    }

    public List<BaseUser> findAllBaseUsers() {
        List<BaseUser> list = new ArrayList<>();

        String sql = "SELECT id, username, email, password, type FROM users ORDER BY id";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new BaseUser(
                        rs.getLong("id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("type")
                ));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load base users", e);
        }

        return list;
    }

    public Optional<String> findUserType(long id) {
        String sql = "SELECT type FROM users WHERE id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) return Optional.of(rs.getString("type"));

        } catch (SQLException e) {
            throw new RuntimeException("Failed to get type for id " + id, e);
        }

        return Optional.empty();
    }
}
