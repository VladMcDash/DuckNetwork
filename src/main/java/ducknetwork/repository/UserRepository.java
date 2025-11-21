package ducknetwork.repository;

import ducknetwork.domain.User;
import ducknetwork.domain.Person;
import ducknetwork.domain.Duck;
import ducknetwork.exceptions.DomainExceptions;
import ducknetwork.persistence.Database;
import ducknetwork.util.Validators;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Comparator;
import java.util.stream.Collectors;

public class UserRepository {

    private final PersonRepository personRepo = new PersonRepository();
    private final DuckRepository duckRepo = new DuckRepository();

    public User save(User user) {
        if (user == null)
            throw new IllegalArgumentException("User cannot be null");

        Validators.validate(Validators.USERNAME_VALIDATOR, user.getUsername());
        Validators.validate(Validators.EMAIL_VALIDATOR, user.getEmail());

        String sql = "INSERT INTO users(username, email, password, type) VALUES (?, ?, ?, ?) RETURNING id";
        Connection conn = null;

        try {
            conn = Database.getInstance().getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, user.getUsername());
                ps.setString(2, user.getEmail());
                ps.setString(3, user.getPassword());
                ps.setString(4, (user instanceof Duck) ? "DUCK" : "PERSON");

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        long newId = rs.getLong("id");
                        user.setId(newId);
                    } else {
                        conn.rollback();
                        throw new RuntimeException("Failed to retrieve generated user id");
                    }
                }
            }


            User result;
            if (user instanceof Person p) {
                personRepo.saveDetails(conn, p);
                result = p;
            } else if (user instanceof Duck d) {
                duckRepo.saveDetails(conn, d);
                result = d;
            } else {
                conn.rollback();
                throw new IllegalArgumentException("Unsupported User subtype: " + user.getClass());
            }

            conn.commit();
            return result;

        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignore) {}
            }
            if (e instanceof SQLException sqlE && "23505".equals(sqlE.getSQLState())) {
                throw new RuntimeException("Duplicate username or email: " + sqlE.getMessage(), sqlE);
            }
            throw new RuntimeException("Error adding user: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignore) {}
            }
        }
    }

    public void delete(long id) {
        if (findById(id) == null) {
            throw new DomainExceptions.UserNotFoundException("User with id " + id + " not found");
        }

        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete user " + id, e);
        }
    }

    public User findById(Long id) {
        Optional<Person> p = personRepo.findById(id);
        if (p.isPresent()) return p.get();
        Optional<Duck> d = duckRepo.findById(id);
        if (d.isPresent()) return d.get();
        return null;
    }

    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        users.addAll(personRepo.findAll());
        users.addAll(duckRepo.findAll());
        return users.stream()
                .sorted(Comparator.comparing(User::getId))
                .collect(Collectors.toList());
    }

    public boolean existsById(Long id) {
        String sql = "SELECT 1 FROM users WHERE id = ?";

        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to check user existence for id " + id, e);
        }
    }
}