package nl.tudelft.sem.users.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import nl.tudelft.sem.users.entities.User;
import nl.tudelft.sem.users.entities.UserRole;
import nl.tudelft.sem.users.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Controller class for users that provides the API.
 */
@RestController
@RequestMapping("/api/users")
@SuppressWarnings("PMD")
public class UserController {

    private final transient UserService userService;

    private final transient ObjectMapper mapper = new ObjectMapper();


    /**
     * Instantiates a new User controller object.
     *
     * @param userService the user service
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }


    /**
     * Registers a user if (s)he does not already exist.
     *
     * @param req the HTTP request
     * @param res the HTTP response
     * @return the user ID of a new registered user
     *         if there is no user with the same username (netID) already
     * @throws IOException when something goes wrong with servlets
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    long registerUser(HttpServletRequest req, HttpServletResponse res) throws IOException {
        JsonNode jsonNode = mapper.readTree(req.getInputStream());
        String username = jsonNode.get("username").asText();
        String firstName = jsonNode.get("firstName").asText();
        String lastName = jsonNode.get("lastName").asText();
        //String password = jsonNode.get("password").asText();

        long userId = this.userService.registerUser(username, firstName, lastName);
        if (userId == -1) {
            String reason = String.format("User with NetID %s already exists!", username);
            throw new ResponseStatusException(HttpStatus.CONFLICT, reason);
        }

        // TODO: register with Authentication Server

        return userId;
    }

    /**
     * Gets a user by their username.
     *
     * @param req the HTTP request
     * @param res the HTTP response
     * @return the user with the given username if they exist
     * @throws IOException when something goes wrong with servlets
     */
    @GetMapping("/by_username")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    String getUserByUsername(HttpServletRequest req, HttpServletResponse res) throws IOException {
        JsonNode jsonNode = mapper.readTree(req.getInputStream());
        String netId = jsonNode.get("username").asText();

        Optional<User> user = this.userService.getUserByNetId(netId);
        if (user.isEmpty()) {
            String reason = String.format("User with NetID %s not found!", netId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, reason);
        }
        return new ObjectMapper().writeValueAsString(user.get());
    }

    /**
     * Gets a user by their user ID.
     *
     * @param req the HTTP request
     * @param res the HTTP response
     * @return the user with the given user ID if they exist
     * @throws IOException when something goes wrong with servlets
     */
    @GetMapping("/by_userid")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    String getUserByUserId(HttpServletRequest req, HttpServletResponse res) throws IOException {
        JsonNode jsonNode = mapper.readTree(req.getInputStream());

        long userId = parseUserId(jsonNode.get("user_id").asText());
        if (userId == -1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Provided user ID is not a valid number");
        }

        Optional<User> user = this.userService.getUserByUserId(userId);
        if (user.isEmpty()) {
            String reason = String.format("User with user ID %s not found!", userId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, reason);
        }
        return new ObjectMapper().writeValueAsString(user.get());
    }

    /**
     * Gets users by the role.
     *
     * @param req the HTTP request
     * @param res the HTTP response
     * @return the users with the given role
     * @throws IOException when something goes wrong with servlets
     */
    @GetMapping("/by_role")
    public @ResponseBody
    String getUsersByRole(HttpServletRequest req, HttpServletResponse res) throws IOException {
        JsonNode jsonNode = mapper.readTree(req.getInputStream());
        String role = jsonNode.get("role").asText();

        // TODO: in case role = CANDIDATE_TA, LECTURER or ADMIN, check for permissions

        List<User> users = this.userService.getUsersByRole(UserRole.valueOf(role));
        if (users.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    String.format("No users having role '%s' are found!", role));
        }
        return new ObjectMapper().writeValueAsString(users);
    }


    /**
     * Changes the role of a user given their netID, if the requester has permissions for that.
     *
     * @param req the HTTP request
     * @param res the HTTP response
     * @return success message, if the request has been successful
     * @throws IOException when something goes wrong with servlets
     */
    @PutMapping("/change_role")
    public @ResponseBody
    String changeRole(HttpServletRequest req, HttpServletResponse res) throws IOException {
        JsonNode jsonNode = mapper.readTree(req.getInputStream());

        long userId = parseUserId(jsonNode.get("userId").asText());
        if (userId == -1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Provided user ID is not a valid number");
        }

        String newRoleStr = jsonNode.get("newRole").asText();
        UserRole newRole = parseRole(newRoleStr);
        if (newRole == null) {
            String reason = String.format("Role must be one of the following: %s, %s, %s, %s, %s",
                    "STUDENT", "CANDIDATE_TA", "TA", "LECTURER", "ADMIN");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, reason);
        }

        //TODO: get the requester's role from the JWT token
        UserRole requesterRole = UserRole.STUDENT;

        Optional<User> optUser = this.userService.getUserByUserId(userId);
        if (optUser.isEmpty()) {
            String reason = String.format("User with user ID %s not found!", userId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, reason);
        }

        UserRole oldRole = optUser.get().getRole();

        boolean success = this.userService.changeRole(userId, newRole, requesterRole);
        if (!success) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Operation not allowed!");
        }

        // TODO: send a request to the Authentication server

        return String.format("The role of user with user ID %s has been changed from %s to %s.",
                             userId, oldRole, newRole);
    }


    /**
     * Deletes a user by their username (netID).
     *
     * @param req the HTTP request
     * @param res the HTTP response
     * @return success message, if the request has been successful
     * @throws IOException when something goes wrong with servlets
     */
    @DeleteMapping("/delete")
    public @ResponseBody
    String deleteUserByUsername(HttpServletRequest req,
                                HttpServletResponse res)throws IOException {
        JsonNode jsonNode = mapper.readTree(req.getInputStream());

        long userId = parseUserId(jsonNode.get("userId").asText());
        if (userId == -1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Provided user ID is not a valid number");
        }

        // TODO: get the role from the token
        UserRole requesterRole = UserRole.STUDENT;

        boolean notFound = this.userService.getUserByUserId(userId).isEmpty();
        if (notFound) {
            String reason = String.format("User with user ID %s not found!", userId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, reason);
        }

        boolean success = this.userService.deleteUserByUserId(userId, requesterRole);
        if (!success) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Operation not allowed!");
        }
        return String.format("The user with the user ID %s has been deleted successfully!", userId);
    }

    /**
     * A helper method to parse userID from string to long.
     *
     * @param userIdStr the user ID string
     * @return the user ID long, or -1 if impossible to parse
     */
    private long parseUserId(String userIdStr) {
        try {
            return Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * A helper method to parse the user role from string to UserRole.
     *
     * @param roleStr   the user role string
     * @return the user role UserRole, or null if the provided role has been invalid
     */
    private UserRole parseRole(String roleStr) {
        try {
            return UserRole.valueOf(roleStr);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
