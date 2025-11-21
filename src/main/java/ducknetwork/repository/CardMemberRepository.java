package ducknetwork.repository;

import ducknetwork.domain.Card;
import ducknetwork.exceptions.DomainExceptions;
import ducknetwork.persistence.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CardMemberRepository {

    private final DuckRepository duckRepo = new DuckRepository();
    private final CardRepository cardRepo = new CardRepository();

    public void addDuckToCard(Long duckId, Long cardId) {
        // Existence checks
        Optional<Card> cardOpt = cardRepo.findById(cardId);
        if (duckRepo.findById(duckId).isEmpty()) {
            throw new DomainExceptions.UserNotFoundException("Duck not found");
        }
        if (cardOpt.isEmpty()) {
            throw new RuntimeException("Card not found");
        }

        String sql = "INSERT INTO duck_card_memberships (duck_id, card_id) VALUES (?, ?) ON CONFLICT (duck_id, card_id) DO NOTHING";

        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, duckId);
            ps.setLong(2, cardId);
            ps.executeUpdate();

        } catch (SQLException e) {
            // CORECTIE: Includem mesajul specific al erorii SQL
            throw new RuntimeException("Failed to add duck " + duckId + " to card " + cardId + ": " + e.getMessage(), e);
        }
    }

    public void removeDuckFromCard(Long duckId, Long cardId) {
        if (duckRepo.findById(duckId).isEmpty()) {
            throw new DomainExceptions.UserNotFoundException("Duck not found");
        }

        String sql = "DELETE FROM duck_card_memberships WHERE duck_id = ? AND card_id = ?";

        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, duckId);
            ps.setLong(2, cardId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to remove duck " + duckId + " from card " + cardId + ": " + e.getMessage(), e);
        }
    }

    public List<Long> findDucksInCard(Long cardId) {
        List<Long> list = new ArrayList<>();

        String sql = """
                SELECT duck_id 
                FROM duck_card_memberships 
                WHERE card_id = ?
                """;

        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, cardId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getLong("duck_id"));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load ducks in card " + cardId + ": " + e.getMessage(), e);
        }

        return list;
    }
}