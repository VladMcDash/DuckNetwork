package ducknetwork.repository;

import ducknetwork.domain.Card;
import ducknetwork.persistence.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Handles persistence operations for the Card entity.
 * (Logic moved from Repo.java and CardDao)
 */
public class CardRepository {

    /**
     * Creates a new card and returns the persisted object.
     */
    public Card save(String name) {
        String sql = "INSERT INTO cards (name) VALUES (?) RETURNING id";

        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long id = rs.getLong("id");
                    return new Card(id, name);
                } else {
                    throw new RuntimeException("Failed to retrieve generated card id");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert card", e);
        }
    }

    /**
     * Remove card by id.
     */
    public void delete(Long cardId) {
        String sql = "DELETE FROM cards WHERE id = ?";

        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, cardId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete card " + cardId, e);
        }
    }

    /**
     * Find card by id.
     */
    public Optional<Card> findById(Long cardId) {
        String sql = "SELECT id, name FROM cards WHERE id = ?";

        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, cardId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Card(
                            rs.getLong("id"),
                            rs.getString("name")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find card " + cardId, e);
        }
        return Optional.empty();
    }

    /**
     * List all cards.
     */
    public List<Card> findAll() {
        List<Card> cards = new ArrayList<>();
        String sql = "SELECT id, name FROM cards ORDER BY id";

        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                cards.add(new Card(
                        rs.getLong("id"),
                        rs.getString("name")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load cards", e);
        }
        return cards;
    }
}