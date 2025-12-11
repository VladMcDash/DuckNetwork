package ducknetwork.repository;

import ducknetwork.domain.*;
import ducknetwork.persistence.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DuckRepository {


    public void saveDetails(Connection conn, Duck d) throws SQLException {
        if (d.getId() == null)
            throw new RuntimeException("Duck must have user_id before saving details");

        String sql = "INSERT INTO duck_details(user_id, type, speed, endurance) " +
                "VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, d.getId());
            ps.setString(2, d.getType());
            ps.setDouble(3, d.getSpeed());
            ps.setDouble(4, d.getEndurance());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error saving duck details for id " + d.getId() + ": " + e.getMessage(), e);
        }
    }

    public Optional<Duck> findById(Long id) {
        String sql = """
                SELECT u.id, u.username, u.email, u.password, 
                       d.type, d.speed, d.endurance
                FROM users u 
                JOIN duck_details d ON d.user_id = u.id 
                WHERE u.id = ? AND u.type = 'DUCK'
                """;

        try (Connection conn = Database.getInstance().getConnection();
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

                Duck d = createDuckInstance(uid, username, email, password, speed, endurance, type);

                return Optional.of(d);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error finding duck by id " + id, e);
        }
    }


    public List<Duck> findAll() {
        return findPage("TOATE", 1, 1000).getContent();
    }

    /**
     * Numara toate rațele.
     */
    public long countAll(String typeFilter) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) AS total FROM users u JOIN duck_details d ON d.user_id = u.id WHERE u.type = 'DUCK'");

        List<String> params = new ArrayList<>();
        if (typeFilter != null && !typeFilter.equalsIgnoreCase("TOATE")) {
            sql.append(" AND d.type = ?");
            params.add(typeFilter);
        }

        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement statement = conn.prepareStatement(sql.toString())) {

            for(int i = 0; i < params.size(); i++) {
                statement.setString(i + 1, params.get(i));
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getLong("total");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting ducks with filter " + typeFilter, e);
        }
        return 0;
    }

    /**
     * Returneaza o pagina de rațe, opțional filtrata dupa tip.
     */
    public Page<Duck> findPage(String typeFilter, int pageNumber, int pageSize) {
        if (pageNumber <= 0 || pageSize <= 0) {
            throw new IllegalArgumentException("Page number and size must be positive.");
        }

        long totalElements = countAll(typeFilter);
        int offset = (pageNumber - 1) * pageSize;

        // Ajusteaza pagina
        if (offset >= totalElements && totalElements > 0) {
            int totalPages = (int) Math.ceil((double) totalElements / pageSize);
            pageNumber = Math.max(1, totalPages);
            offset = (pageNumber - 1) * pageSize;
        }

        StringBuilder sql = new StringBuilder("""
                SELECT u.id, u.username, u.email, u.password, 
                       d.type, d.speed, d.endurance 
                FROM users u 
                JOIN duck_details d ON d.user_id = u.id 
                WHERE u.type = 'DUCK'
                """);

        List<Object> params = new ArrayList<>();
        if (typeFilter != null && !typeFilter.equalsIgnoreCase("TOATE")) {
            sql.append(" AND d.type = ?");
            params.add(typeFilter);
        }

        sql.append(" ORDER BY u.id LIMIT ? OFFSET ?");

        //paginare
        params.add(pageSize);
        params.add(offset);

        List<Duck> list = new ArrayList<>();

        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for(int i = 0; i < params.size(); i++) {
                if (params.get(i) instanceof Integer) {
                    ps.setInt(i + 1, (Integer) params.get(i));
                } else if (params.get(i) instanceof String) {
                    ps.setString(i + 1, (String) params.get(i));
                } else {
                    ps.setObject(i + 1, params.get(i));
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Long id = rs.getLong("id");
                    String username = rs.getString("username");
                    String email = rs.getString("email");
                    String password = rs.getString("password");

                    String type = rs.getString("type");
                    double speed = rs.getDouble("speed");
                    double endurance = rs.getDouble("endurance");

                    Duck d = createDuckInstance(id, username, email, password, speed, endurance, type);

                    list.add(d);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error reading paginated ducks with filter " + typeFilter + ": " + e.getMessage(), e);
        }

        return new Page<>(list, totalElements, pageNumber, pageSize);
    }

    private Duck createDuckInstance(Long id, String username, String email, String password,
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