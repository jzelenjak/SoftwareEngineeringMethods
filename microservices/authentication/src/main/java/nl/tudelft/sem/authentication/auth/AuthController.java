package nl.tudelft.sem.authentication.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.tudelft.sem.authentication.exceptions.UserAlreadyExistsException;
import nl.tudelft.sem.authentication.jwt.JwtTokenProvider;
import nl.tudelft.sem.authentication.jwt.JwtTokenUtil;
import nl.tudelft.sem.authentication.repository.UserDataRepository;
import nl.tudelft.sem.authentication.security.AuthenticationSecurityConfig;
import org.h2.engine.User;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final transient ObjectMapper objectMapper = new ObjectMapper();
    private final JwtTokenUtil jwtTokenUtil;
    private final UserDataRepository users;

    /**
     * Controls the authentication.
     *
     * @param authService     the authentication service.
     */
    public AuthController(AuthService authService, JwtTokenUtil jwtTokenUtil, UserDataRepository users) {
        this.authService = authService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.users = users;
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
//        JsonNode jsonNode = objectMapper.readTree(body);
//        String username = jsonNode.get("username").asText();
//        String newPassword = jsonNode.get("password").asText();
//
//        // TODO: Check if user must be authenticated already
//        this.authService.changePassword(username, newPassword);

        return "Password changed successfully!";
    }


    @GetMapping("/login")
    public String login(@RequestBody String body) throws JsonProcessingException {
        JsonNode jsonNode = objectMapper.readTree(body);
        String username = jsonNode.get("username").asText();
        String token = JwtTokenProvider.createToken(username, this.users.findByUsername(username).get().getRole());
        return token;
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
     * Gets jwt token util.
     *
     * @return jwt token util.
     */
    public JwtTokenUtil getJwtTokenUtil() {
        return this.jwtTokenUtil;
    }

    /**
     * Gets users.
     *
     * @return users.
     */
    public UserDataRepository getUsers() {
        return this.users;
    }
}
