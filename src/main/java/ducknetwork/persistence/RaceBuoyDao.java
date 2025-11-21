package ducknetwork.persistence;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
//pentru balize de cursa si distantele lor
public class RaceBuoyDao {

    public void insertBuoy(long raceEventId, double distance, int position) {
        String sql = """
                INSERT INTO race_buoys (race_event_id, distance, position)
                VALUES (?, ?, ?)
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, raceEventId);
            ps.setDouble(2, distance);
            ps.setInt(3, position);

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert buoy", e);
        }
    }

    public List<Double> getBuoys(long raceEventId) {
        List<Double> list = new ArrayList<>();

        String sql = """
                SELECT distance
                FROM race_buoys
                WHERE race_event_id = ?
                ORDER BY position
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, raceEventId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(rs.getDouble("distance"));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load buoys", e);
        }

        return list;
    }

    public void deleteAllForRace(long raceEventId) {
        String sql = "DELETE FROM race_buoys WHERE race_event_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, raceEventId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete buoys", e);
        }
    }
}
