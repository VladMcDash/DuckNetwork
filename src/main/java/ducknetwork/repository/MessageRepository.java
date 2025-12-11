package ducknetwork.repository;

import ducknetwork.domain.Message;
import ducknetwork.domain.ReplyMessage;
import ducknetwork.domain.User;
import ducknetwork.persistence.Database;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MessageRepository {
    private final UserRepository userRepo = new UserRepository();

    public void save(Message message) {
        String sqlMessage = "INSERT INTO messages(from_user_id, message_text, date_sent, reply_id) VALUES (?, ?, ?, ?) RETURNING id";
        String sqlRecipients = "INSERT INTO message_recipients(message_id, to_user_id) VALUES (?, ?)";

        Connection conn = null;
        try {
            conn = Database.getInstance().getConnection();
            conn.setAutoCommit(false);

            long msgId;
            try (PreparedStatement ps = conn.prepareStatement(sqlMessage)) {
                ps.setLong(1, message.getFrom().getId());
                ps.setString(2, message.getMessage());
                ps.setTimestamp(3, Timestamp.valueOf(message.getDate()));
                if (message.getReply() != null) {
                    ps.setLong(4, message.getReply().getId());
                } else {
                    ps.setNull(4, Types.BIGINT);
                }

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        msgId = rs.getLong("id");
                        message.setId(msgId);
                    } else {
                        throw new SQLException("Failed to insert message.");
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(sqlRecipients)) {
                for (User recipient : message.getTo()) {
                    ps.setLong(1, msgId);
                    ps.setLong(2, recipient.getId());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            throw new RuntimeException("Error saving message", e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }
    }

    public List<Message> findConversation(Long user1Id, Long user2Id) {
        List<Message> messages = new ArrayList<>();
        String sql = """
            SELECT m.id, m.from_user_id, m.message_text, m.date_sent, m.reply_id
            FROM messages m
            JOIN message_recipients mr ON m.id = mr.message_id
            WHERE (m.from_user_id = ? AND mr.to_user_id = ?)
               OR (m.from_user_id = ? AND mr.to_user_id = ?)
            ORDER BY m.date_sent ASC
        """;

        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, user1Id);
            ps.setLong(2, user2Id);
            ps.setLong(3, user2Id);
            ps.setLong(4, user1Id);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Long id = rs.getLong("id");
                    Long fromId = rs.getLong("from_user_id");
                    String text = rs.getString("message_text");
                    LocalDateTime date = rs.getTimestamp("date_sent").toLocalDateTime();
                    Long replyId = rs.getObject("reply_id", Long.class);

                    User from = userRepo.findById(fromId);
                    User to = (fromId.equals(user1Id)) ? userRepo.findById(user2Id) : userRepo.findById(user1Id);

                    Message msg;
                    if (replyId != null) {
                        Message original = findById(replyId);
                        msg = new ReplyMessage(id, from, List.of(to), text, date, original);
                    } else {
                        msg = new Message(id, from, List.of(to), text, date);
                    }
                    messages.add(msg);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching conversation", e);
        }
        return messages;
    }

    public Message findById(Long id) {
        String sql = "SELECT * FROM messages WHERE id = ?";
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if(rs.next()) {
                    Long fromId = rs.getLong("from_user_id");
                    String text = rs.getString("message_text");
                    LocalDateTime date = rs.getTimestamp("date_sent").toLocalDateTime();
                    User from = userRepo.findById(fromId);
                    return new Message(id, from, new ArrayList<>(), text, date); // Lista 'to' e goala aici pt optimizare
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}