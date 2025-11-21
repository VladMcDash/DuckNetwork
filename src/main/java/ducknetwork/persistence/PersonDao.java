package ducknetwork.persistence;

import ducknetwork.domain.Person;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO pentru entitatea Person (detaliile unei persoane).
 */
public class PersonDao {

    private final UserDao userDao = new UserDao();

    /**
     * Creeaza un user de tip PERSON + intrarea in person_details.
     * Intoarce obiectul Person cu ID setat.
     */
    public Person save(Person p) {
        if (p.getId() == null)
            throw new RuntimeException("Person must have user_id before saving details");

        String sql = "INSERT INTO person_details(user_id, first_name, last_name, birth_date, occupation, empathy) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, p.getId());
            ps.setString(2, p.getFirstName());
            ps.setString(3, p.getLastName());
            ps.setDate(4, (p.getBirthDate() != null) ? java.sql.Date.valueOf(p.getBirthDate()) : null);
            ps.setString(5, p.getOccupation());
            ps.setInt(6, p.getEmpathy());

            ps.executeUpdate();
            return p;

        } catch (SQLException e) {
            throw new RuntimeException("Error saving person details", e);
        }
    }


    /**
     * Gasește o persoana dupa ID.
     * Reconstruiește obiectul complet Person.
     */
    public Optional<Person> findById(long id) {

        Optional<UserDao.BaseUser> baseOpt = userDao.findAllBaseUsers().stream()
                .filter(b -> b.id() == id)
                .findFirst();

        if (baseOpt.isEmpty()) return Optional.empty();
        UserDao.BaseUser base = baseOpt.get();

        if (!"PERSON".equals(base.type()))
            return Optional.empty();

        String sql = """
                SELECT first_name, last_name, birth_date, occupation, empathy
                FROM person_details
                WHERE user_id = ?
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) return Optional.empty();

            Person p = new Person(
                    id,
                    base.username(),
                    base.email(),
                    base.password(),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getObject("birth_date", java.time.LocalDate.class),
                    rs.getString("occupation"),
                    rs.getObject("empathy", Integer.class)
            );

            return Optional.of(p);

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load Person details", e);
        }
    }

    /**
     * toate persoanele din DB.
     * Folosit în Repo.loadFromDatabase()
     */
    public List<Person> findAll() {
        List<Person> persons = new ArrayList<>();

        // toți userii
        List<UserDao.BaseUser> baseUsers = new UserDao().findAllBaseUsers();

        // filtrăm doar persoanele
        for (UserDao.BaseUser base : baseUsers) {
            if (!"PERSON".equals(base.type())) continue;

            long id = base.id();

            String sql = """
                    SELECT first_name, last_name, birth_date, occupation, empathy
                    FROM person_details
                    WHERE user_id = ?
                    """;

            try (Connection conn = Database.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setLong(1, id);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    Person p = new Person(
                            id,
                            base.username(),
                            base.email(),
                            base.password(),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getObject("birth_date", java.time.LocalDate.class),
                            rs.getString("occupation"),
                            rs.getObject("empathy", Integer.class)
                    );
                    persons.add(p);
                }

            } catch (SQLException e) {
                throw new RuntimeException("Failed to load person with id " + id, e);
            }
        }

        return persons;
    }

    /**
     * Șterge o persoană pe baza ID.
     *   ON DELETE CASCADE din tabela users face automat asta.
     */
    public void delete(long id) {
        userDao.delete(id);
    }
}
