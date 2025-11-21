
package ducknetwork.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Abstract representation of a user in the social network.
 * Contains common fields and friendship management.
 */
public abstract class User {
    /**
     * Unique identifier of the user.
     */
    protected Long id;

    /**
     * Login name.
     */
    protected String username;

    /**
     * Contact email address.
     */
    protected String email;

    /**
     * Account password.
     */
    protected String password;

    /**
     * Local list of friends. Repository manages bidirectional consistency.
     */
    protected final List<User> friends = new ArrayList<>();

    /**
     * Construct a new user.
     *
     * @param id       unique identifier
     * @param username login name
     * @param email    contact email
     * @param password account password
     */
    public User(Long id, String username, String email, String password) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
    }

    /**
     * Add a friend to the local friend list.
     * Repository is responsible for ensuring bidirectional updates; this method
     * only protects against nulls and duplicates.
     *
     * @param u friend to add; if null the method is a no-op
     */
    public void addFriend(User u) {
        if (u == null) return;
        if (!friends.contains(u)) friends.add(u);
    }

    /**
     * Remove a friend from the local friend list.
     *
     * @param u friend to remove
     */
    public void removeFriend(User u) {
        friends.remove(u);
    }

    /**
     * Get an unmodifiable view of friends.
     *
     * @return unmodifiable list of friends
     */
    public List<User> getFriends() {
        return Collections.unmodifiableList(friends);
    }

    /**
     * Get the user id.
     *
     * @return id of the user
     */
    public Long getId() { return id; }

    /**
     * Get the username.
     *
     * @return username/login name
     */
    public String getUsername() { return username; }

    /**
     * Get the email address.
     *
     * @return contact email
     */
    public String getEmail() { return email; }

    /**
     * String representation including concrete class name, id, username and email.
     *
     * @return formatted string representation
     */
    @Override
    public String toString() {
        return String.format("%s{id=%d, username=%s, email=%s}", getClass().getSimpleName(), id, username, email);
    }

    /**
     * Users are considered equal when their ids are equal.
     *
     * @param o other object
     * @return true if same instance or ids are equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    /**
     * Hash code based on the user id.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


}
