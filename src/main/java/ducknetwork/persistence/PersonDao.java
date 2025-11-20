package ducknetwork.persistence;

import ducknetwork.domain.Person;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PersonDao {

    public Person save(Person p) {
        String sql = "INSERT INTO person (username, email, password, first_name, last_name, birth_date, occupation, empathy) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, p.getUsername());
            stmt.setString(2, p.getEmail());
            stmt.setString(3, p.getPassword());
            stmt.setString(4, p.getFirstName());
            stmt.setString(5, p.getLastName());

            if (p.getBirthDate() != null) {
                stmt.setDate(6, Date.valueOf(p.getBirthDate()));
            } else {
                stmt.setNull(6, Types.DATE);
            }

            stmt.setString(7, p.getOccupation());
            stmt.setInt(8, p.getEmpathy());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Long id = rs.getLong("id");
                    return new Person(id, p.getUsername(), p.getEmail(), p.getPassword(),
                            p.getFirstName(), p.getLastName(), p.getBirthDate(),
                            p.getOccupation(), p.getEmpathy());
                } else {
                    throw new SQLException("Insert returned no id");
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<Person> findById(long id) {
        String sql = "SELECT * FROM person WHERE id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Person> findAll() {
        String sql = "SELECT * FROM person ORDER BY id";
        List<Person> result = new ArrayList<>();

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

    public void update(Person p) {
        if (p.getId() == null) {
            throw new IllegalArgumentException("Person id required for update");
        }

        String sql = "UPDATE person SET username = ?, email = ?, password = ?, first_name = ?, last_name = ?, birth_date = ?, occupation = ?, empathy = ? WHERE id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, p.getUsername());
            stmt.setString(2, p.getEmail());
            stmt.setString(3, p.getPassword());
            stmt.setString(4, p.getFirstName());
            stmt.setString(5, p.getLastName());

            if (p.getBirthDate() != null) {
                stmt.setDate(6, Date.valueOf(p.getBirthDate()));
            } else {
                stmt.setNull(6, Types.DATE);
            }

            stmt.setString(7, p.getOccupation());
            stmt.setInt(8, p.getEmpathy());
            stmt.setLong(9, p.getId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(long id) {
        String sql = "DELETE FROM person WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    private Person mapRow(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        String username = rs.getString("username");
        String email = rs.getString("email");
        String password = rs.getString("password");
        String firstName = rs.getString("first_name");
        String lastName = rs.getString("last_name");

        Date bd = rs.getDate("birth_date");
        LocalDate birthDate = (bd != null) ? bd.toLocalDate() : null;

        String occupation = rs.getString("occupation");
        int empathy = rs.getInt("empathy");
        if (rs.wasNull()) {
            empathy = 0;
        }

        return new Person(id, username, email, password, firstName, lastName, birthDate, occupation, empathy);
    }

}
