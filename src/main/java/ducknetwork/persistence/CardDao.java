package ducknetwork.persistence;

import ducknetwork.domain.Card;
import ducknetwork.persistence.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CardDao {

    public long insertCard(String name) {
        String sql = "INSERT INTO cards(name) VALUES (?) RETURNING id";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong("id");
            }

            throw new RuntimeException("Failed to insert card");

        } catch (SQLException e) {
            throw new RuntimeException("Error in insertCard", e);
        }
    }

    public void deleteCard(Long id) {
        String sql = "DELETE FROM cards WHERE id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting card", e);
        }
    }
    public List<Card> findAll() {
        List<Card> list = new ArrayList<>();
        String sql = "SELECT id, name FROM cards ORDER BY id";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                long id = rs.getLong("id");
                String name = rs.getString("name");

                list.add(new Card(id, name));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error reading cards", e);
        }

        return list;
    }
}
