package nl.tudelft.sem.users.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import nl.tudelft.sem.jwt.JwtUtils;
import nl.tudelft.sem.users.config.GatewayConfig;
import nl.tudelft.sem.users.entities.User;
import nl.tudelft.sem.users.entities.UserRole;
import nl.tudelft.sem.users.services.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Controller class for users that provides `retrieve` (query) API.
 */
@RestController
@RequestMapping("/api/users")
public class UserQueryController extends UserBaseController {

    /**
     * Instantiates a new User controller object.
     *
     * @param userService   the user service object
     * @param jwtUtils      the utilities for JWT
     * @param gatewayConfig the configuration for the gateway
     */
    public UserQueryController(UserService userService, JwtUtils jwtUtils,
                               GatewayConfig gatewayConfig) {
        super(userService, jwtUtils, gatewayConfig);
    }

    /**
     * Gets a user by their username. Is only allowed for lecturers and admins.
     *
     * @param req   the HTTP request
     * @return the user with the given username if they exist.
     *         If the user with the provided username does not exist in the database,
     *           then 404 NOT FOUND status is sent back.
     * @throws IOException when something goes wrong with servlets
     */
    @GetMapping("/by_username")
    public ResponseEntity<String> getByUsername(HttpServletRequest req) throws IOException {
        String prefixedJwt = req.getHeader(HttpHeaders.AUTHORIZATION);
        validate(prefixedJwt, Set.of(UserRole.LECTURER.name(), UserRole.ADMIN.name()));

        String username = req.getParameter("username");
        Optional<User> user = this.userService.getUserByUsername(username);
        if (user.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                String.format("User with the username %s not found!", username));
        }

        String json = new ObjectMapper().writeValueAsString(user.get());
        return new ResponseEntity<>(json, HttpStatus.OK);
    }

    /**
     * Gets a user by their user ID. Is only allowed for lecturers and admins.
     *
     * @param req   the HTTP request
     * @return the user with the given user ID if they exist.
     *         If the user does not exist, then 404 NOT FOUND status is sent.
     * @throws IOException when something goes wrong with servlets
     */
    @GetMapping("/by_userid")
    public ResponseEntity<String> getByUserId(HttpServletRequest req) throws IOException {
        String prefixedJwt = req.getHeader(HttpHeaders.AUTHORIZATION);
        validate(prefixedJwt, Set.of(UserRole.LECTURER.name(), UserRole.ADMIN.name()));

        long userId = parseUserId(req.getParameter("userId"));
        String json = new ObjectMapper().writeValueAsString(getUserByUserId(userId));
        return new ResponseEntity<>(json, HttpStatus.OK);
    }

    /**
     * Gets users by the role. Is only allowed for lecturers and admins.
     *
     * @param req   the HTTP request
     * @return the users with the given role.
     *         If no users are found, then 404 NOT FOUND is sent back.
     * @throws IOException when something goes wrong with servlets
     */
    @GetMapping("/by_role")
    public ResponseEntity<String> getByRole(HttpServletRequest req) throws IOException {
        String prefixedJwt = req.getHeader(HttpHeaders.AUTHORIZATION);
        validate(prefixedJwt, Set.of(UserRole.LECTURER.name(), UserRole.ADMIN.name()));

        UserRole role = parseRole(req.getParameter("role").toUpperCase(Locale.ROOT));
        List<User> users = this.userService.getUsersByRole(role);
        if (users.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                String.format("No users having role '%s' are found!", role));
        }
        String json = new ObjectMapper().writeValueAsString(users);
        return new ResponseEntity<>(json, HttpStatus.OK);
    }

    /**
     * Gets users by first name. Only allowed by admin.
     *
     * @param req the HTTP request.
     * @return the users with the given first name.
     *         If no users are found, then 404 NOT FOUND is sent back.
     * @throws IOException when something goes wrong with servlets
     */
    @GetMapping("/by_first_name")
    public ResponseEntity<String> getByFirstName(HttpServletRequest req) throws IOException {
        String prefixedJwt = req.getHeader(HttpHeaders.AUTHORIZATION);
        validate(prefixedJwt, Set.of(UserRole.ADMIN.name()));

        final String firstName = req.getParameter(FIRSTNAME);
        Optional<List<User>> users = this.userService.getUsersByFirstName(firstName);
        if (users.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                String.format("No users having first name '%s' found!", firstName));
        }
        String json = new ObjectMapper().writeValueAsString(users.get());
        return new ResponseEntity<>(json, HttpStatus.OK);
    }

    /**
     * Gets users by last name. Only allowed by admin.
     *
     * @param req the HTTP request.
     * @return the users with the given last name.
     *         If no users are found, then 404 NOT FOUND is sent back.
     * @throws IOException when something goes wrong with servlets
     */
    @GetMapping("/by_last_name")
    public ResponseEntity<String> getByLastName(HttpServletRequest req) throws IOException {
        String prefixedJwt = req.getHeader(HttpHeaders.AUTHORIZATION);
        validate(prefixedJwt, Set.of(UserRole.ADMIN.name()));

        final String lastName = req.getParameter(LASTNAME);
        Optional<List<User>> users = this.userService.getUsersByLastName(lastName);
        if (users.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                String.format("No users having last name '%s' found!", lastName));
        }
        String json = new ObjectMapper().writeValueAsString(users.get());
        return new ResponseEntity<>(json, HttpStatus.OK);
    }

    /**
     * Teach people not to make sus requests.
     *
     * @return a simple yet famous and very important message
     */
    @GetMapping("/admin")
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public @ResponseBody
    String kindaSus() {
        return "Kinda sus, ngl!";
    }
}
