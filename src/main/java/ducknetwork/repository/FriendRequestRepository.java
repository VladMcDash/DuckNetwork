package ducknetwork.repository;

import ducknetwork.domain.FriendRequestDTO;
import ducknetwork.persistence.Database;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FriendRequestRepository {

    public void save(Long fromId, Long toId) {
        String checkSql = "SELECT status FROM friend_requests WHERE from_user_id = ? AND to_user_id = ?";
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
            psCheck.setLong(1, fromId);
            psCheck.setLong(2, toId);
            try (ResultSet rs = psCheck.executeQuery()) {
                if (rs.next()) {
                    if ("REJECTED".equals(rs.getString("status"))) {
                        updateStatus(fromId, toId, "PENDING");
                        return;
                    } else {
                        throw new RuntimeException("Exista deja o cerere activa!");
                    }
                }
            }
            String sql = "INSERT INTO friend_requests(from_user_id, to_user_id, status, date_sent) VALUES (?, ?, 'PENDING', ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, fromId);
                ps.setLong(2, toId);
                ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                ps.executeUpdate();
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public void updateStatus(Long fromId, Long toId, String status) {
        String sql = "UPDATE friend_requests SET status = ? WHERE from_user_id = ? AND to_user_id = ?";
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setLong(2, fromId);
            ps.setLong(3, toId);
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public List<FriendRequestDTO> findAllRequestsForUser(Long userId, UserRepository userRepo) {
        List<FriendRequestDTO> list = new ArrayList<>();
        String sql = "SELECT from_user_id, to_user_id, status, date_sent FROM friend_requests WHERE from_user_id = ? OR to_user_id = ?";
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Long fromId = rs.getLong("from_user_id");
                    Long toId = rs.getLong("to_user_id");
                    String status = rs.getString("status");
                    LocalDateTime date = rs.getTimestamp("date_sent").toLocalDateTime();

                    String label;
                    if (fromId.equals(userId)) {
                        label = "SENT TO: " + userRepo.findById(toId).getEmail();
                    } else {
                        label = "RECEIVED FROM: " + userRepo.findById(fromId).getEmail();
                    }
                    list.add(new FriendRequestDTO(label, status, date));
                }
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return list;
    }
}