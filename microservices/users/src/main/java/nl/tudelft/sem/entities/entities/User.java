package nl.tudelft.sem.entities.entities;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;


/**
 * A class that represents a user.
 */
@Table(name = "users")
@Entity
@SequenceGenerator(name = "uid_gen", sequenceName = "uid_seq",
        initialValue = 3001001, allocationSize = 7)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "uid_gen")
    @Column(name = "user_id", updatable = false)
    private long userId;

    @Column(name = "username", unique = true)
    @NotNull @NotBlank
    // NetID of the user
    private String username;

    @Column(name = "first_name")
    @NotNull @NotBlank
    private String firstName;

    @Column(name = "last_name")
    @NotNull @NotBlank
    private String lastName;

    @Column(name = "role")
    private UserRole role;

    /**
     * Instantiates a User object.
     *
     * @param username  the username of the user.
     * @param firstName the first name of the user.
     * @param lastName  the last name of the user.
     * @param role      the role of the user.
     */
    public User(String username, String firstName,
                String lastName, UserRole role) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
    }

    /**
     * Instantiates a User object.
     */
    public User() {
    }

    /**
     * Gets the user id.
     *
     * @return the user id
     */
    public long getUserId() {
        return this.userId;
    }

    /**
     * Sets the user id.
     * Cannot be updated (otherwise DataIntegrityViolationException will be thrown).
     *
     * @param userId the new user id
     */
    public void setUserId(long userId) {
        this.userId = userId;
    }

    /**
     * Gets the username of the user.
     *
     * @return the username of the user
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Sets the username of the user.
     * Must be unique (otherwise DataIntegrityViolationException will be thrown).
     *
     * @param username the username of the user
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the first name of the user.
     *
     * @return the first name of the user
     */
    public String getFirstName() {
        return this.firstName;
    }

    /**
     * Sets the first name of the user.
     *
     * @param firstName the first name of the user
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Gets the last name of the user.
     *
     * @return the last name of the user
     */
    public String getLastName() {
        return this.lastName;
    }

    /**
     * Sets the last name of the user.
     *
     * @param lastName the last name of the user
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Gets the role of the user.
     *
     * @return the role of the user
     */
    public UserRole getRole() {
        return this.role;
    }

    /**
     * Sets the role of the user.
     *
     * @param role the role of the user
     */
    public void setRole(UserRole role) {
        this.role = role;
    }

    /**
     * Checks if another object is equal to this object.
     *
     * @param other the other object to compare to
     * @return true if the other object is also a User and if their user IDs are equal,
     *         false otherwise
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof User)) {
            return false;
        }

        return this.userId == ((User) other).userId;
    }

    /**
     * Returns the hash code of a User object.
     *
     * @return the hash code of a User object
     */
    @Override
    public int hashCode() {
        return Long.valueOf(this.userId).hashCode();
    }
}
