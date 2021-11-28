package nl.tudelft.sem.authentication.jwt;

public class CredentialsAuthenticationRequest {

    private String username;
    private String password;

    public CredentialsAuthenticationRequest() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
