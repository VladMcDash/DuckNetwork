package ducknetwork.repository;

import ducknetwork.domain.Person;
import ducknetwork.persistence.Database;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PersonRepository {

    public void saveDetails(Connection conn, Person p) throws SQLException {
        if (p.getId() == null)
            throw new RuntimeException("Person must have user_id before saving details");

        String sql = "INSERT INTO person_details(user_id, first_name, last_name, birth_date, occupation, empathy) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, p.getId());
            ps.setString(2, p.getFirstName());
            ps.setString(3, p.getLastName());
            ps.setDate(4, (p.getBirthDate() != null) ? java.sql.Date.valueOf(p.getBirthDate()) : null);
            ps.setString(5, p.getOccupation());
            ps.setObject(6, p.getEmpathy(), Types.INTEGER);


            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error saving person details for id " + p.getId() + ": " + e.getMessage(), e);
        }
    }

    public Optional<Person> findById(long id) {
        String sql = """
                SELECT u.id, u.username, u.email, u.password, 
                       p.first_name, p.last_name, p.birth_date, p.occupation, p.empathy
                FROM users u 
                JOIN person_details p ON p.user_id = u.id 
                WHERE u.id = ? AND u.type = 'PERSON'
                """;

        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();

                Person p = new Person(
                        rs.getLong("id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getObject("birth_date", LocalDate.class),
                        rs.getString("occupation"),
                        rs.getObject("empathy", Integer.class)
                );

                return Optional.of(p);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load Person details for id " + id, e);
        }
    }

    public List<Person> findAll() {
        List<Person> persons = new ArrayList<>();

        String sql = """
                SELECT u.id, u.username, u.email, u.password, 
                       p.first_name, p.last_name, p.birth_date, p.occupation, p.empathy
                FROM users u 
                JOIN person_details p ON p.user_id = u.id 
                WHERE u.type = 'PERSON'
                ORDER BY u.id
                """;

        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Person p = new Person(
                        rs.getLong("id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getObject("birth_date", LocalDate.class),
                        rs.getString("occupation"),
                        rs.getObject("empathy", Integer.class)
                );
                persons.add(p);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load all Persons", e);
        }

        return persons;
    }
}