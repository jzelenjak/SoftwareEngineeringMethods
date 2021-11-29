package nl.tudelft.sem.authentication.jwt;


/**
 * A class for an authentication request using credentials.
 */
public class CredentialsAuthenticationRequest {
    private String username;
    private String password;

    /**
     * Instantiates a new CredentialsAuthenticationRequest.
     */
    public CredentialsAuthenticationRequest() {
    }

    /**
     * Gets the username.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username.
     *
     * @param username the username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the password.
     *
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password.
     *
     * @param password the password
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
