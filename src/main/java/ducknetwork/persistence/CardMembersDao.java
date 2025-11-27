package ducknetwork.persistence;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CardMembersDao {

    public void addDuckToCard(long duckId, long cardId) {
        String sql = """
                INSERT INTO duck_card_memberships (duck_id, card_id)
                VALUES (?, ?)
                ON CONFLICT DO NOTHING
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, duckId);
            ps.setLong(2, cardId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to add duck to card", e);
        }
    }

    public void removeDuckFromCard(long duckId, long cardId) {
        String sql = """
                DELETE FROM duck_card_memberships
                WHERE duck_id = ? AND card_id = ?
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, duckId);
            ps.setLong(2, cardId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to remove duck from card", e);
        }
    }

    public List<Long> findDucksInCard(long cardId) {
        List<Long> list = new ArrayList<>();

        String sql = """
                SELECT duck_id 
                FROM duck_card_memberships 
                WHERE card_id = ?
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, cardId);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(rs.getLong("duck_id"));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load ducks in card", e);
        }

        return list;
    }
}
