package ducknetwork.repository;

import ducknetwork.persistence.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RaceBuoyRepository {

    public void saveBuoys(Connection conn, long eventId, List<Double> buoys) throws SQLException {
        String sql = "INSERT INTO race_buoys (race_event_id, distance, position) VALUES (?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int pos = 1;
            for (Double d : buoys) {
                ps.setLong(1, eventId);
                ps.setDouble(2, d);
                ps.setInt(3, pos++);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public List<Double> findBuoys(Connection conn, long eventId) throws SQLException {
        List<Double> buoys = new ArrayList<>();
        String sql = "SELECT distance FROM race_buoys WHERE race_event_id = ? ORDER BY position";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    buoys.add(rs.getDouble("distance"));
                }
            }
        }
        return buoys;
    }

    public List<Double> findBuoys(long eventId) {
        try (Connection conn = Database.getInstance().getConnection()) {
            return findBuoys(conn, eventId);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find buoys for event " + eventId + ": " + e.getMessage(), e);
        }
    }
}