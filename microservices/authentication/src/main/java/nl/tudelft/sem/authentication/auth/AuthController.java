package nl.tudelft.sem.authentication.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.tudelft.sem.authentication.exceptions.UserAlreadyExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
public class AuthController {
    private final transient AuthService authService;
    private final transient ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Controls the authentication.
     *
     //* @param authenticationManager the authentication manager
     * @param authService     the authentication service.
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
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
        //String password = jsonNode.get("password").asText();
        String newPassword = jsonNode.get("new_password").asText();

        // TODO: Check if user must be authenticated already
        this.authService.changePassword(username, newPassword);

        return "Password changed successfully!";
    }


    @GetMapping("/authenticate")
    public int authenticate(@RequestBody String body) throws JsonProcessingException {
        return -1;
    }
}
