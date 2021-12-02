package nl.tudelft.sem.entities.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.tudelft.sem.entities.entities.User;
import nl.tudelft.sem.entities.entities.UserRole;
import nl.tudelft.sem.entities.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Controller class for users.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final transient UserService userService;


    public UserController(UserService userService) {
        this.userService = userService;
    }


    @PostMapping("/register")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody String registerUser(HttpServletRequest req,
                                             HttpServletResponse res) throws IOException {
        JsonNode jsonNode = new ObjectMapper().readTree(req.getInputStream());
        String netId = jsonNode.get("username").asText();
        String firstName = jsonNode.get("first_name").asText();
        String lastName = jsonNode.get("last_name").asText();

        long userId = this.userService.registerUser(netId, firstName, lastName, UserRole.STUDENT);
        if (userId == -1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    String.format("User with NetID %s already exists!", netId));
        }
        return String.format("UserId : %d", userId);
    }

    @GetMapping("/by_username")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody String getUserByUsername(HttpServletRequest req,
                                        HttpServletResponse res) throws IOException {
        JsonNode jsonNode = new ObjectMapper().readTree(req.getInputStream());
        String netId = jsonNode.get("username").asText();

        Optional<User> user = this.userService.getUserByNetId(netId);
        if (user.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    String.format("User with NetID %s not found!", netId));
        }
        return new ObjectMapper().writeValueAsString(user.get());
    }

    @GetMapping("/by_user_id")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody String getUserByUserId(HttpServletRequest req,
                                                  HttpServletResponse res) throws IOException {
        JsonNode jsonNode = new ObjectMapper().readTree(req.getInputStream());
        long userId = 0;

        try {
            userId = Long.parseLong(jsonNode.get("user_id").asText());
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Provided user ID '%s' is not a valid number", userId));
        }

        Optional<User> user = this.userService.getUserByUserId(userId);
        if (user.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    String.format("User with user ID %s not found!", userId));
        }
        return new ObjectMapper().writeValueAsString(user.get());
    }

    @GetMapping("/by_role")
    public @ResponseBody String getUsersByRole(HttpServletRequest req,
                                        HttpServletResponse res) throws IOException {
        JsonNode jsonNode = new ObjectMapper().readTree(req.getInputStream());
        String role = jsonNode.get("role").asText();

        List<User> users = this.userService.getUsersByRole(UserRole.valueOf(role));
        if (users.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    String.format("No users havind role %s are found!", role));
        }
        return new ObjectMapper().writeValueAsString(users);
    }
    // TODO To add: delete, promote, send request to authentication
}
