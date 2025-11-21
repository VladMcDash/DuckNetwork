package ducknetwork.persistence;

import ducknetwork.domain.*;
import ducknetwork.persistence.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DuckDao {

    public Duck save(Duck d) {
        if (d.getId() == null)
            throw new RuntimeException("Duck must have user_id before saving details");

        String sql = "INSERT INTO duck_details(user_id, type, speed, endurance, card_id) " +
                "VALUES (?, ?, ?, ?, NULL)";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, d.getId());
            ps.setString(2, d.getType());
            ps.setDouble(3, d.getSpeed());
            ps.setDouble(4, d.getEndurance());

            ps.executeUpdate();
            return d;

        } catch (SQLException e) {
            throw new RuntimeException("Error saving duck details", e);
        }
    }


    public void delete(Long id) {
        String sql = "DELETE FROM duck_details WHERE user_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting duck details", e);
        }
    }

   public Optional<Duck> findById(Long id) {
        String sql =
                "SELECT u.id, u.username, u.email, u.password, " +
                        "       d.type, d.speed, d.endurance " +
                        "FROM users u " +
                        "JOIN duck_details d ON d.user_id = u.id " +
                        "WHERE u.id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();

                Long uid = rs.getLong("id");
                String username = rs.getString("username");
                String email = rs.getString("email");
                String password = rs.getString("password");

                String type = rs.getString("type");
                double speed = rs.getDouble("speed");
                double endurance = rs.getDouble("endurance");

                Duck d = buildDuck(uid, username, email, password, speed, endurance, type);

                return Optional.of(d);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error finding duck by id", e);
        }
    }

    public List<Duck> findAll() {
        List<Duck> list = new ArrayList<>();

        String sql =
                "SELECT u.id, u.username, u.email, u.password, " +
                        "       d.type, d.speed, d.endurance " +
                        "FROM users u " +
                        "JOIN duck_details d ON d.user_id = u.id " +
                        "ORDER BY u.id";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Long id = rs.getLong("id");
                String username = rs.getString("username");
                String email = rs.getString("email");
                String password = rs.getString("password");

                String type = rs.getString("type");
                double speed = rs.getDouble("speed");
                double endurance = rs.getDouble("endurance");

                Duck d = buildDuck(id, username, email, password, speed, endurance, type);

                list.add(d);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error reading ducks", e);
        }

        return list;
    }

    private Duck buildDuck(Long id, String username, String email, String password,
                           double speed, double endurance, String type) {

        switch (type) {
            case "SWIMMING":
                return new SwimmingDuck(id, username, email, password, speed, endurance);

            case "FLYING":
                return new FlyingDuck(id, username, email, password, speed, endurance);

            case "FLYING_AND_SWIMMING":
                return new FlyingAndSwimmingDuck(id, username, email, password, speed, endurance);

            default:
                throw new RuntimeException("Unknown duck type in DB: " + type);
        }
    }
}
