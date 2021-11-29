package nl.tudelft.sem.authentication.auth;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import nl.tudelft.sem.authentication.security.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


/**
 * A class for storing user information.
 */
@Entity
@Table
public class UserData implements UserDetails {
    @Id
    @Column
    private String username;

    @Column
    private String password;

    @Column
    private UserRole role;

    @Column
    private final boolean accountNonExpired;

    @Column
    private final boolean accountNonLocked;

    @Column
    private final boolean credentialsNonExpired;

    @Column
    private final boolean enabled;

    @Column
    private static final long serialVersionUID = 25546278L;

    /**
     * Instantiates a new UserData class.
     *
     * @param username              the username of the user
     * @param password              the password  of the user
     * @param role                  the role of the user
     */
    public UserData(String username, String password, UserRole role) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.accountNonExpired = true;
        this.accountNonLocked = true;
        this.credentialsNonExpired = true;
        this.enabled = true;
    }

    /**
     * Gets granted authorities.
     *
     * @return the granted authorities
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority(this.role.name()));
        return authorities;
    }

    /**
     * Gets the password.
     *
     * @return the password
     */
    @Override
    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the username.
     *
     * @return the username
     */
    @Override
    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the role of the user.
     *
     * @return the UserRole.
     */
    public UserRole getRole() {
        return this.role;
    }

    /**
     * Gets the password.
     *
     * @return the password
     */
    public void setRole(UserRole role) {
        this.role = role;
    }

    /**
     * Checks if the account is not expired.
     *
     * @return true if the account is not expired, false otherwise
     */
    @Override
    public boolean isAccountNonExpired() {
        return this.accountNonExpired;
    }

    /**
     * Checks if the account is not locked.
     *
     * @return true if the account is not locked, false otherwise
     */
    @Override
    public boolean isAccountNonLocked() {
        return this.accountNonLocked;
    }

    /**
     * Checks if the credentials are not expired.
     *
     * @return true if the credentials are not expired, false otherwise
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return this.credentialsNonExpired;
    }

    /**
     * Checks if the user is enabled.
     *
     * @return true if the user is enabled, false otherwise
     */
    @Override
    public boolean isEnabled() {
        return this.enabled;
    }
}
