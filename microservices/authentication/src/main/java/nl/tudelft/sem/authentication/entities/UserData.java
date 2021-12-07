package nl.tudelft.sem.authentication.entities;

import java.util.Collection;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import nl.tudelft.sem.authentication.security.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


/**
 * A class for storing user data.
 * Username (netID) is used to log in or register a user
 *      (it is the identifier in authentication microservice).
 * UserId is mostly used by other microservices to identify the users,
 *      also it is used as "Subject" in JWT token.
 */
@Entity(name = "user_data")
public class UserData implements UserDetails {
    @Id
    @Column(name = "username", length = 50, unique = true, nullable = false)
    private String username;

    @Column(name = "userId", unique = true, nullable = false, updatable = false)
    private long userId;

    @Column(name = "password", length = 128, nullable = false)
    private String password;

    @Column(name = "role", nullable = false)
    private UserRole role;

    @Column(name = "account_non_expired")
    private boolean accountNonExpired;

    @Column(name = "account_non_locked")
    private boolean accountNonLocked;

    @Column(name = "credentials_non_expired")
    private boolean credentialsNonExpired;

    @Column(name = "enabled")
    private boolean enabled;

    private static final long serialVersionUID = 25565543525446278L;

    /**
     * An empty constructor to create a user.
     */
    public UserData(){

    }

    /**
     * Instantiates a new UserData class.
     *
     * @param username              the netID of the user
     * @param password              the password of the user
     * @param role                  the role of the user
     * @param userId                the user ID of the user
     */
    public UserData(String username, String password, UserRole role, long userId) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.userId = userId;
        this.accountNonExpired = true;
        this.accountNonLocked = true;
        this.credentialsNonExpired = true;
        this.enabled = true;
    }

    /**
     * Gets the granted authorities of the user.
     *
     * @return the granted authorities of the user
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Set.of(new SimpleGrantedAuthority(this.role.name()));
    }

    /**
     * Gets the (hashed) password of the user.
     *
     * @return the (hashed) password of the user
     */
    @Override
    public String getPassword() {
        return this.password;
    }

    /**
     * Sets the password of the user.
     *
     * @param password the password of the user
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the username (net ID) of the user.
     *
     * @return the username of the user
     */
    @Override
    public String getUsername() {
        return this.username;
    }

    /**
     * Sets the username (net ID) of the user.
     *
     * @param username the username of the user
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the user ID of the user.
     *
     * @return the user ID of the user.
     */
    public long getUserId() {
        return this.userId;
    }

    /**
     * Sets the user ID of the user.
     *
     * @param userId the user ID of the user.
     */
    public void setUserId(long userId) {
        this.userId = userId;
    }

    /**
     * Gets the role of the user.
     *
     * @return the role of the user.
     */
    public UserRole getRole() {
        return this.role;
    }

    /**
     * Sets the role of the user.
     *
     * @param role the new role of the user
     */
    public void setRole(UserRole role) {
        this.role = role;
    }

    /**
     * Checks if the account of the user is not expired.
     *
     * @return true if the account of the user is not expired, false otherwise
     */
    @Override
    public boolean isAccountNonExpired() {
        return this.accountNonExpired;
    }

    /**
     * Sets the account of the user to be either expired or not.
     *
     * @param accountNonExpired whether the account of the user is not expired
     */
    public void setAccountNonExpired(boolean accountNonExpired) {
        this.accountNonExpired = accountNonExpired;
    }

    /**
     * Checks if the account of the user is not locked.
     *
     * @return true if the account of the user is not locked, false otherwise
     */
    @Override
    public boolean isAccountNonLocked() {
        return this.accountNonLocked;
    }

    /**
     * Sets the account of the user to be either locked or not.
     *
     * @param accountNonLocked whether the account of the user is not locked
     */
    public void setAccountNonLocked(boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }

    /**
     * Checks if the credentials of the user are not expired.
     *
     * @return true if the credentials of the user are not expired, false otherwise
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return this.credentialsNonExpired;
    }

    /**
     * Sets the credentials of the user to be either expired tot not.
     *
     * @param credentialsNonExpired whether the credentials of the user are not expired
     */
    public void setCredentialsNonExpired(boolean credentialsNonExpired) {
        this.credentialsNonExpired = credentialsNonExpired;
    }

    /**
     * Checks if the user is enabled (a disabled user cannot be authenticated).
     *
     * @return true if the user is enabled, false otherwise
     */
    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Sets the user to be enabled or not.
     *
     * @param enabled whether the user is enabled
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Checks if another user (UserData) is equals to this user.
     *
     * @param other the object ot compare to
     * @return true if the other object is also an instance of UserData class
     *         and if the usernames match, otherwise false
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof UserData)) {
            return false;
        }

        UserData that = (UserData) other;
        return this.username.equals(that.username);
    }

    /**
     * Returns the hash code of a user (UserData) object.
     *
     * @return the hash code of a user (UserData) object.
     */
    @Override
    public int hashCode() {
        return this.username.hashCode();
    }
}
