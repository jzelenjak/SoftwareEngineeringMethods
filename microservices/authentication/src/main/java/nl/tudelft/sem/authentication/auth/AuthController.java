package nl.tudelft.sem.authentication.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.tudelft.sem.authentication.exceptions.UserAlreadyExistsException;
import nl.tudelft.sem.authentication.jwt.JwtTokenProvider;
import nl.tudelft.sem.authentication.repository.UserDataRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final transient ObjectMapper objectMapper = new ObjectMapper();
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDataRepository users;
    private final AuthenticationManager authenticationManager;

    /**
     * Controls the authentication.
     *
     * @param authService     the authentication service.
     * @param authenticationManager
     */
    public AuthController(AuthService authService, JwtTokenProvider jwtTokenProvider,
                          UserDataRepository users, AuthenticationManager authenticationManager) {
        this.authService = authService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.users = users;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Registers a new user to the system, if not already.
     *
     * @param body JSON body with username and password
     * @return true if successful.
     * @throws UserAlreadyExistsException if the user already exists.
     */
    @PostMapping("/register")
    public String register(@RequestBody String body) throws JsonProcessingException, UserAlreadyExistsException {
        JsonNode jsonNode = objectMapper.readTree(body);
        String uname = jsonNode.get("username").asText();
        this.authService.registerUser(uname, jsonNode.get("password").asText());
        return String.format("Greetings to %s from registration", jsonNode.get("username").asText());
    }


    /**
     * PLACEHOLDER JAVADOC.
     * Allows the user to change their own credentials if authorized.
     *
     * @param body JSON body with the credentials and new password
     *
     * @return message for now.
     */
    @PutMapping("/change_password")
    public String changePassword(@RequestBody String body) throws JsonProcessingException {
        JsonNode jsonNode = objectMapper.readTree(body);
        String username = jsonNode.get("username").asText();
        String password = jsonNode.get("password").asText();
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));
        String newPassword = jsonNode.get("new_password").asText();

        this.authService.changePassword(username, newPassword);

        return "Password changed successfully!";
    }


    @GetMapping("/login")
    public String login(@RequestBody String body) throws JsonProcessingException {
        try {
            JsonNode jsonNode = objectMapper.readTree(body);
            String username = jsonNode.get("username").asText();
            String password = jsonNode.get("password").asText();
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
            String token = jwtTokenProvider.createToken(username, this.users.findByUsername(username).get().getRole());
            return token;
        } catch (Exception e) {
            throw new IllegalArgumentException("Incorrect credentials");
        }

    }

    /**
     * Gets auth service.
     *
     * @return auth service.
     */
    public AuthService getAuthService() {
        return this.authService;
    }

    /**
     * Gets jwt provider.
     *
     * @return jwt token provider.
     */
    public JwtTokenProvider getJwtTokenProvider() {
        return this.jwtTokenProvider;
    }

    /**
     * Gets users.
     *
     * @return users.
     */
    public UserDataRepository getUsers() {
        return this.users;
    }

    /**
     * Gets authentication manager.
     *
     * @return authentication manager.
     */
    public AuthenticationManager getAuthenticationManager() {
        return this.authenticationManager;
    }
}
