package ducknetwork.persistence;

import ducknetwork.domain.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DuckDao {

    public Duck save(Duck d) {
        String sql = "INSERT INTO duck (username, email, type, speed, endurance) VALUES (?, ?, ?, ?, ?) RETURNING id";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, d.getUsername());
            stmt.setString(2, d.getEmail());
            stmt.setString(3, getTypeName(d));
            stmt.setDouble(4, d.getSpeed());
            stmt.setDouble(5, d.getEndurance());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) d.setId(rs.getLong("id"));

            return d;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Duck> findAll() {
        List<Duck> result = new ArrayList<>();
        String sql = "SELECT * FROM duck ORDER BY id";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                result.add(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    private Duck mapRow(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        String username = rs.getString("username");
        String email = rs.getString("email");
        String type = rs.getString("type");
        double speed = rs.getDouble("speed");
        double endurance = rs.getDouble("endurance");

        return switch (type) {
            case "SWIMMING" -> new SwimmingDuck(id, username, email," ", speed, endurance);
            case "FLYING" -> new FlyingDuck(id, username, email, " ",speed, endurance);
            case "FLYING_AND_SWIMMING" -> new FlyingAndSwimmingDuck(id, username, email," ", speed, endurance);
            default -> new SwimmingDuck(id, username, email," ", speed, endurance);
        };
    }

    private String getTypeName(Duck d) {
        if (d instanceof SwimmingDuck) return "SWIMMING";
        if (d instanceof FlyingDuck) return "FLYING";
        if (d instanceof FlyingAndSwimmingDuck) return "FLYING_AND_SWIMMING";
        return "SWIMMING";
    }
    public void delete(long id) {
        String sql = "DELETE FROM duck WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
