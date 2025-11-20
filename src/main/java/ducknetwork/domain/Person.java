package ducknetwork.domain;

import java.time.LocalDate;

/**
 * Human user with additional personal fields.
 */
public class Person extends User {
    private final String firstName;
    private final String lastName;
    private final LocalDate birthDate;
    private final String occupation;
    private final int empathy;

    /**
     * Create a new Person.
     *
     * @param id         unique identifier of the user
     * @param username   login name
     * @param email      contact email
     * @param password   account password
     * @param firstName  given name
     * @param lastName   family name
     * @param birthDate  date of birth
     * @param occupation current occupation
     * @param empathy    empathy score (integer)
     */
    public Person(Long id, String username, String email, String password,
                  String firstName, String lastName, LocalDate birthDate,
                  String occupation, int empathy) {
        super(id, username, email, password);
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.occupation = occupation;
        this.empathy = empathy;
    }

    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public LocalDate getBirthDate() { return birthDate; }
    public String getOccupation() { return occupation; }
    public int getEmpathy() { return empathy; }

    // Delegate accessors to the inherited fields (id/username/email/password are provided by User)
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }

    public String getFullName() { return firstName + " " + lastName; }

    @Override
    public String toString() {
        return String.format("Person{id=%d, username=%s, name=%s %s, occupation=%s}", id, username, firstName, lastName, occupation);
    }
}
