package nl.tudelft.sem.users.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

/**
 * Controller class for users that provides the API.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final transient UserService userService;

    private final transient JwtUtils jwtUtils;

    private final transient WebClient webClient;

    private final transient GatewayConfig gatewayConfig;

    private static final transient String USERNAME = "username";
    private static final transient String FIRSTNAME = "firstName";
    private static final transient String LASTNAME = "lastName";
    private static final transient String USERID = "userId";
    private static final transient String ROLE = "role";


    /**
     * Instantiates a new User controller object.
     *
     * @param userService       the user service object
     * @param jwtUtils          the utilities for JWT
     * @param gatewayConfig     the configuration for the gateway
     */
    public UserController(UserService userService, JwtUtils jwtUtils, GatewayConfig gatewayConfig) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
        this.webClient = WebClient.create();
        this.gatewayConfig = gatewayConfig;
    }


    /**
     * Registers a user if (s)he does not already exist in the database. Sends an HTTP request
     *      to the Authentication microservice to register the user in its database.
     *
     * @param req   the HTTP request
     * @return If there is no user with the same username already,
     *              the user ID of a new registered user and 200 OK is sent back.
     *         If the user with the provided username already exists in the database,
     *              then 409 CONFLICT status is sent back.
     */
    @PostMapping("/register")
    public Mono<ResponseEntity<String>> registerUser(HttpServletRequest req) throws IOException {
        JsonNode jsonNode = new ObjectMapper().readTree(req.getInputStream());
        String username = parseJsonField(jsonNode, USERNAME);
        String firstName = parseJsonField(jsonNode, "firstName");
        String lastName = parseJsonField(jsonNode, "lastName");
        String password = parseJsonField(jsonNode, "password");

        long userId = attemptToRegister(username, firstName, lastName);
        String json = new ObjectMapper().createObjectNode().put(USERNAME, username)
                .put(USERID, userId).put("password", password).toString();
        return webClient
                .post()
                .uri(buildUri("api", "auth", "register"))
                .header(HttpHeaders.CONTENT_TYPE, String.valueOf(MediaType.APPLICATION_JSON))
                .body(Mono.just(json), String.class)
                .exchange()
                .flatMap(res -> {
                    if (res.statusCode().isError()) {
                        // Fail the registration if the user could not be registered in Auth Server
                        this.userService.deleteUserByUserId(userId);
                        return Mono.error(new ResponseStatusException(res.statusCode(),
                                                "Registration failed!"));
                    }

                    // Just forward the response from Auth Server and add the user ID in the body
                    String body = new ObjectMapper().createObjectNode()
                                                    .put(USERID, userId).toString();
                    return Mono.just(ResponseEntity.status(HttpStatus.OK)
                            .contentType(MediaType.APPLICATION_JSON).body(body));
                });
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
        String json = new ObjectMapper().writeValueAsString(getUserByUsername(username));
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
        validateAdmin(prefixedJwt);

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
        validateAdmin(prefixedJwt);

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
     * Changes the role of a user given their userId. Only allowed for admins.
     *  If the request has been successful, then 200 OK is sent back.
     *  If the user does not exist, then 404 NOT FOUND status is sent.
     *  If the requester is not an admin, then 403 FORBIDDEN status is sent back.
     *
     * @param req the HTTP request
     * @return success message if the request has been successful
     */
    @PutMapping("/change_role")
    public Mono<ResponseEntity<String>> changeRole(HttpServletRequest req) throws IOException {
        String prefixedJwt = req.getHeader(HttpHeaders.AUTHORIZATION);
        validateAdmin(prefixedJwt);

        JsonNode jsonNode = new ObjectMapper().readTree(req.getInputStream());
        long userId = parseUserId(parseJsonField(jsonNode, USERID));
        UserRole newRole = parseRole(parseJsonField(jsonNode, ROLE).toUpperCase(Locale.US));

        String username = this.getUserByUserId(userId).getUsername();
        String json = createJson(USERNAME, username, ROLE, newRole.name());
        return webClient
                .put()
                .uri(buildUri("api", "auth", "change_role"))
                .header(HttpHeaders.CONTENT_TYPE, String.valueOf(MediaType.APPLICATION_JSON))
                .header(HttpHeaders.AUTHORIZATION, prefixedJwt)
                .body(Mono.just(json), String.class)
                .exchange()
                .flatMap(res -> {
                    if (res.statusCode().isError()) {
                        // Do not do anything locally to provide consistency
                        return Mono.error(new ResponseStatusException(res.statusCode(),
                            "Could not change role"));
                    }
                    // Since operation is successful in Auth server, change the role locally as well
                    this.userService.changeRole(userId, newRole);
                    return Mono.just(ResponseEntity.status(HttpStatus.OK)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(createJson("message", "Changed the role successfully!")));
                });
    }

    /**
     * Changes the first name of a user. Only possible by admins.
     *
     * @param req the HTTP request.
     * @throws IOException IO exception if something goes wrong with the servlets.
     */
    @PutMapping("/change_first_name")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void changeFirstName(HttpServletRequest req) throws IOException {
        String prefixedJwt = req.getHeader(HttpHeaders.AUTHORIZATION);
        validateAdmin(prefixedJwt);

        JsonNode jsonNode = new ObjectMapper().readTree(req.getInputStream());
        long userId = parseUserId(parseJsonField(jsonNode, USERID));
        String newFirstName = parseJsonField(jsonNode, FIRSTNAME);
        this.userService.changeFirstName(userId, newFirstName);
    }

    /**
     * Changes the last name of a user. Only possible by admins.
     *
     * @param req the HTTP request.
     * @throws IOException IO exception if something goes wrong with the servlets.
     */
    @PutMapping("/change_last_name")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void changeLastName(HttpServletRequest req) throws IOException {
        String prefixedJwt = req.getHeader(HttpHeaders.AUTHORIZATION);
        validateAdmin(prefixedJwt);

        JsonNode jsonNode = new ObjectMapper().readTree(req.getInputStream());
        long userId = parseUserId(parseJsonField(jsonNode, USERID));
        String newLastName = parseJsonField(jsonNode, LASTNAME);
        this.userService.changeLastName(userId, newLastName);
    }

    /**
     * Deletes a user by their user ID. Only allowed for admins.
     * If the request has been successful, then 200 OK is sent back.
     * If user with the provided user ID has not been found, then 404 NOT FOUND is sent back.
     * If the requester is not an admin, then 403 FORBIDDEN status is sent back.
     *
     * @param req   the HTTP request
     * @return success message if the request has been successful
     */
    @DeleteMapping("/delete")
    public Mono<ResponseEntity<String>> deleteByUserId(HttpServletRequest req) {
        String prefixedJwt = req.getHeader(HttpHeaders.AUTHORIZATION);
        validateAdmin(prefixedJwt);

        long userId = parseUserId(req.getParameter(USERID));
        String username = getUserByUserId(userId).getUsername();
        return webClient
                .method(HttpMethod.DELETE)
                .uri(buildUri("api", "auth", "delete"))
                .header(HttpHeaders.CONTENT_TYPE, String.valueOf(MediaType.APPLICATION_JSON))
                .header(HttpHeaders.AUTHORIZATION, prefixedJwt)
                .body(Mono.just(createJson(USERNAME, username)), String.class)
                .exchange()
                .flatMap(res -> {
                    if (res.statusCode().isError()) {
                        // Do not do anything locally to provide consistency
                        return Mono.error(new ResponseStatusException(res.statusCode(),
                            "Could not delete the user!"));
                    }
                    // Since operation is successful in Auth server, delete the user locally as well
                    this.userService.deleteUserByUserId(userId);
                    return Mono.just(ResponseEntity.status(HttpStatus.OK)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(createJson("message", "User deleted successfully!")));
                });
    }


    /*
     * Helper methods to reduce code duplication.
     */

    /**
     * Helper method to validate admin role.
     *
     * @param jwtPrefixed the JWT token extracted from 'Authorization' header
     */
    private void validateAdmin(String jwtPrefixed) {
        validate(jwtPrefixed, Set.of(UserRole.ADMIN.name()));
    }

    /**
     * A helper method to create a URI for HTTP request.
     *
     * @param path      the path in the url
     * @return the complete String url
     */
    private String buildUri(String... path) {
        return UriComponentsBuilder.newInstance().scheme("http")
                .host(gatewayConfig.getHost()).port(gatewayConfig.getPort())
                .pathSegment(path).toUriString();
    }

    /**
     * A helper method to create json string out of String key-value pairs.
     *
     * @param kvPairs       list of key-values, must be an even number
     * @return the json string that can be used in the response body
     */
    private String createJson(String... kvPairs) {
        ObjectNode node = new ObjectMapper().createObjectNode();

        for (int i = 0; i < kvPairs.length; i += 2) {
            node.put(kvPairs[i], kvPairs[i + 1]);
        }
        return node.toString();
    }

    /**
     * A helper method to extract the value from a JSON object field
     *   and handle potential exceptions.
     *
     * @param jsonNode  jsonNode with JSON.
     * @param field     the field whose value is to be extracted.
     * @return the extracted value if successful. If not, ResponseStatusException is thrown.
     */
    private String parseJsonField(JsonNode jsonNode, String field) {
        try {
            return jsonNode.get(field).asText();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Error while parsing JSON. The body is corrupted or required fields are missing");
        }
    }

    /**
     * A helper method to parse userID from string to long.
     *
     * @param userIdStr the user ID string
     * @return the user ID (long) if successful. If not, ResponseStatusException is thrown.
     */
    private long parseUserId(String userIdStr) {
        try {
            return Long.parseLong(userIdStr);
        } catch (Exception e) {
            // NumberFormatException or NullPointerException
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Provided user ID is not a valid number");
        }
    }

    /**
     * A helper method to parse the user role from string to UserRole.
     *
     * @param roleStr   the user role string
     * @return the user role (UserRole) if successful. If not, ResponseStatusException is thrown.
     */
    private UserRole parseRole(String roleStr) {
        try {
            return UserRole.valueOf(roleStr);
        } catch (Exception e) {
            // Either IllegalArgumentException or NullPointerException
            String reason = String.format("Role must be one of the following: %s, %s, %s",
                    "STUDENT", "LECTURER", "ADMIN");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, reason);
        }
    }

    /**
     * A helper method to validate the requester using JWT token.
     *
     * @param jwtPrefixed   the JWT token extracted from 'Authorization' header
     * @param allowedRoles  the roles that the token can contain to perform the operation
     */
    private void validate(String jwtPrefixed, Set<String> allowedRoles) {
        String jwt = jwtUtils.resolveToken(jwtPrefixed);

        if (jwt == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "'Authorization' header must start with 'Bearer '");
        }

        Jws<Claims> claimsJws = jwtUtils.validateAndParseClaims(jwt);
        if (claimsJws == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "JWT token is invalid or has been expired");
        }

        if (!allowedRoles.contains(jwtUtils.getRole(claimsJws).toUpperCase(Locale.ROOT))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Operation not allowed!");
        }
    }

    /**
     * A helper method to get a user with the given username if (s)he exists.
     *
     * @param username the username of the user.
     * @return the User with the given username if (s)he exists.
     *          If not, ResponseStatusException is thrown
     */
    private User getUserByUsername(String username) {
        Optional<User> user = this.userService.getUserByUsername(username);
        if (user.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                String.format("User with the username %s not found!", username));
        }
        return user.get();
    }

    /**
     * A helper method to get a user with the given user ID if (s)he exists.
     *
     * @param userId the user ID of the user.
     * @return the User with the given user ID if (s)he exists.
     *         If not, ResponseStatusException is thrown
     */
    private User getUserByUserId(long userId) {
        Optional<User> user = this.userService.getUserByUserId(userId);
        if (user.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                String.format("User with user ID %s not found!", userId));
        }
        return user.get();
    }


    /**
     * A helper method to attempt to register a user and deal with exceptions.
     *
     * @param username  the username of the user.
     * @param firstName the first name of the user.
     * @param lastName  the last name of the user.
     * @return generated user ID for the new user if successful.
     *         If not ResponseStatusException is thrown.
     */
    private long attemptToRegister(String username, String firstName, String lastName) {
        try {
            return this.userService.registerUser(username, firstName, lastName);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }
}
