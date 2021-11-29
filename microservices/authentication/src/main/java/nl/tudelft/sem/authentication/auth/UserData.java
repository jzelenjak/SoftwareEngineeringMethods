package nl.tudelft.sem.authentication.auth;

import nl.tudelft.sem.authentication.security.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


/**
 * A class for storing user information.
 */
public class UserData implements UserDetails {
    private final String username;
    private final String password;
    private final Set<SimpleGrantedAuthority> authorities;
    private final boolean accountNonExpired;
    private final boolean accountNonLocked;
    private final boolean credentialsNonExpired;
    private final boolean enabled;
    private static final long serialVersionUID = 25546278L;

    /**
     * Instantiates a new UserData class.
     *
     * @param username              the username of the user
     * @param password              the password  of the user
     * @param role                  the role of the user
     * @param accountNonExpired     whether the account is not expired
     * @param accountNonLocked      whether the account is not locked
     * @param credentialsNonExpired whether the credentials are not expired
     * @param enabled               whether the user is enabled
     */
    public UserData(String username, String password, UserRole role, boolean accountNonExpired,
                    boolean accountNonLocked, boolean credentialsNonExpired, boolean enabled) {
        this.username = username;
        this.password = password;
        this.authorities = new HashSet<>();
        this.authorities.add(new SimpleGrantedAuthority(role.name()));
        this.accountNonExpired = accountNonExpired;
        this.accountNonLocked = accountNonLocked;
        this.credentialsNonExpired = credentialsNonExpired;
        this.enabled = enabled;
    }

    /**
     * Gets granted authorities.
     * @return the granted authorities
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    /**
     * Gets the password.
     * @return the password
     */
    @Override
    public String getPassword() {
        return this.password;
    }

    /**
     * Gets the username.
     * @return the username
     */
    @Override
    public String getUsername() {
        return this.username;
    }

    /**
     * Checks if the account is not expired.
     * @return true if the account is not expired, false otherwise
     */
    @Override
    public boolean isAccountNonExpired() {
        return this.accountNonExpired;
    }

    /**
     * Checks if the account is not locked.
     * @return true if the account is not locked, false otherwise
     */
    @Override
    public boolean isAccountNonLocked() {
        return this.accountNonLocked;
    }

    /**
     * Checks if the credentials are not expired.
     * @return true if the credentials are not expired, false otherwise
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return this.credentialsNonExpired;
    }

    /**
     * Checks if the user is enabled.
     * @return true if the user is enabled, false otherwise
     */
    @Override
    public boolean isEnabled() {
        return this.enabled;
    }
}
