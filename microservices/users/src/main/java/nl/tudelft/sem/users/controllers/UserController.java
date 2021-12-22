package nl.tudelft.sem.users.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
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
     * Registers a user if (s)he does not already exist in the database.
     * Sends an HTTP request to the Authentication microservice to register the user
     *   in its database as well and get the JWT token that is sent in the
     *   'Authorization' header in the HTTP response.
     *
     * @param req   the HTTP request
     * @return If there is no user with the same username (netID) already,
     *              the user ID of a new registered user and 200 OK is sent back.
     *           If the user with the provided netID already exists in the database,
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
                .flatMap(response -> {
                    if (response.statusCode().isError()) {
                        // Fail the registration if the user could not be registered in Auth Server
                        this.userService.deleteUserByUserId(userId, UserRole.ADMIN);
                        String reason = "Registration failed!";
                        return Mono.error(new ResponseStatusException(response.statusCode(),
                                reason));
                    }

                    // Just forward the response from Auth Server and add the user ID in the body
                    String body = new ObjectMapper().createObjectNode()
                            .put(USERID, userId).toString();
                    return Mono.just(ResponseEntity.status(HttpStatus.OK)
                            .contentType(MediaType.APPLICATION_JSON).body(body));
                });
    }


    /**
     * Gets a user by their username (netID).
     *
     * @param req   the HTTP request
     * @return the user with the given username if they exist.
     *         If the user with the provided netID does not exist in the database,
     *           then 404 NOT FOUND status is sent back.
     * @throws IOException when something goes wrong with servlets
     */
    @GetMapping("/by_username")
    public ResponseEntity<String> getByUsername(HttpServletRequest req) throws IOException {
        String username = req.getParameter("username");
        String json = new ObjectMapper().writeValueAsString(getUserByUsername(username));
        return new ResponseEntity<>(json, HttpStatus.OK);
    }


    /**
     * Gets a user by their user ID.
     *
     * @param req   the HTTP request
     * @return the user with the given user ID if they exist.
     *         If the user does not exist, then 404 NOT FOUND status is sent.
     *         If the provided user ID is not a number, then 400 BAD REQUEST is sent back.
     * @throws IOException when something goes wrong with servlets
     */
    @GetMapping("/by_userid")
    public ResponseEntity<String> getByUserId(HttpServletRequest req) throws IOException {
        long userId = parseUserId(req.getParameter("userId"));
        String json = new ObjectMapper().writeValueAsString(getUserByUserId(userId));
        return new ResponseEntity<>(json, HttpStatus.OK);
    }

    /**
     * Gets users by the role.
     *
     * @param req   the HTTP request
     * @return the users with the given role.
     *         If no users are found, then 404 NOT FOUND is sent back.
     * @throws IOException when something goes wrong with servlets
     */
    @GetMapping("/by_role")
    public ResponseEntity<String> getByRole(HttpServletRequest req) throws IOException {
        UserRole role = parseRole(req.getParameter("role").toUpperCase(Locale.ROOT));
        List<User> users = this.userService.getUsersByRole(role);
        if (users.isEmpty()) {
            String reason = String.format("No users having role '%s' are found!", role);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, reason);
        }
        String json = new ObjectMapper().writeValueAsString(users);
        return new ResponseEntity<>(json, HttpStatus.OK);
    }


    /**
     * Changes the role of a user given their netID, if the requester has permissions for that.
     *  If the request has been successful,
     *      then 200 OK is sent back.
     *  If the provided user ID is not a number or if the new role is invalid,
     *      then 400 BAD REQUEST is sent back.
     *  If the user does not exist,
     *      then 404 NOT FOUND status is sent.
     *  If the operation is not allowed (no privileges),
     *      then 401 UNAUTHORIZED status is sent back.
     *
     * @param req   the HTTP request
     */
    @PutMapping("/change_role")
    public Mono<ResponseEntity<String>> changeRole(HttpServletRequest req) throws IOException {
        JsonNode jsonNode = new ObjectMapper().readTree(req.getInputStream());

        long userId = parseUserId(parseJsonField(jsonNode, USERID));
        UserRole newRole = parseRole(parseJsonField(jsonNode, ROLE).toUpperCase(Locale.US));

        String prefixedToken = req.getHeader(HttpHeaders.AUTHORIZATION);
        Jws<Claims> claimsJws = parseAndValidateJwt(prefixedToken);
        UserRole requesterRole = parseRole(claimsJws);

        // If the requester is not allowed to change the role, send back 401 status
        if (!this.userService.isAllowedToChangeRole(userId, newRole, requesterRole)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Operation not allowed!");
        }

        String username = this.getUserByUserId(userId).getUsername();
        String json = createJson(USERNAME, username, ROLE, newRole.name());
        return webClient
                .put()
                .uri(buildUri("api", "auth", "change_role"))
                .header(HttpHeaders.CONTENT_TYPE, String.valueOf(MediaType.APPLICATION_JSON))
                .header(HttpHeaders.AUTHORIZATION, prefixedToken)
                .body(Mono.just(json), String.class)
                .exchange()
                .flatMap(response -> {
                    if (response.statusCode().isError()) {
                        // Do not do anything locally to provide consistency
                        String reason = "Could not change role";
                        return Mono.error(new ResponseStatusException(response.statusCode(),
                                reason));
                    }
                    // Since operation is successful in Auth server, change the role locally as well
                    this.userService.changeRole(userId, newRole, UserRole.ADMIN);
                    return Mono.just(ResponseEntity.status(HttpStatus.OK)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(createJson("message", "Changed the role successfully!")));
                });
    }


    /**
     * Deletes a user by their user ID.
     * If the request has been successful,
     *      then 200 OK is sent back.
     * If the provided user ID is not a number,
     *      then 400 BAD REQUEST is sent back.
     * If user with the provided user ID has not been found,
     *      then 404 NOT FOUND is sent back.
     * If the requester does not have enough permissions,
     *      then 401 UNAUTHORIZED status is sent back.
     *
     * @param req   the HTTP request
     */
    @DeleteMapping("/delete")
    public Mono<ResponseEntity<String>> deleteByUserId(HttpServletRequest req) {
        String prefixedToken = req.getHeader(HttpHeaders.AUTHORIZATION);
        Jws<Claims> claimsJws = parseAndValidateJwt(prefixedToken);
        UserRole requesterRole = parseRole(claimsJws);

        if (!this.userService.isAllowedToDelete(requesterRole)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Operation not allowed!");
        }

        long userId = parseUserId(req.getParameter(USERID));
        String username = getUserByUserId(userId).getUsername();
        return webClient
                .method(HttpMethod.DELETE)
                .uri(buildUri("api", "auth", "delete"))
                .header(HttpHeaders.CONTENT_TYPE, String.valueOf(MediaType.APPLICATION_JSON))
                .header(HttpHeaders.AUTHORIZATION, prefixedToken)
                .body(Mono.just(createJson(USERNAME, username)), String.class)
                .exchange()
                .flatMap(response -> {
                    if (response.statusCode().isError()) {
                        // Do not do anything locally to provide consistency
                        String reason = "Could not delete the user!";
                        return Mono.error(new ResponseStatusException(response.statusCode(),
                                reason));
                    }
                    // Since operation is successful in Auth server, delete the user locally as well
                    this.userService.deleteUserByUserId(userId, requesterRole);
                    return Mono.just(ResponseEntity.status(HttpStatus.OK)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(createJson("message", "User deleted successfully!")));
                });
    }


    /*
     * Helper methods to reduce code duplication.
     */

    /**
     * A helper method to create a URI for HTTP request.
     *
     * @param path      the path in the url
     * @return the complete String url
     */
    private String buildUri(String... path) {
        return UriComponentsBuilder.newInstance()
                .scheme("http")
                .host(gatewayConfig.getHost())
                .port(gatewayConfig.getPort())
                .pathSegment(path)
                .toUriString();
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
     * @param jsonNode  jsonNode with JSON object.
     * @param field     the field which value is to be extracted.
     * @return the extracted value of the field if successful.
     *         If not, ResponseStatusException is thrown.
     */
    private String parseJsonField(JsonNode jsonNode, String field) {
        try {
            return jsonNode.get(field).asText();
        } catch (Exception e) {
            String reason = "Error while parsing JSON. The body is corrupted"
                    + " or required fields are missing";
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, reason);
        }
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
     * @return the user role UserRole, or null if the provided role has been invalid
     */
    private UserRole parseRole(String roleStr) {
        try {
            return UserRole.valueOf(roleStr);
        } catch (Exception e) {
            // Either IllegalArgumentException or NullPointerException
            String reason = String.format("Role must be one of the following: %s, %s, %s, %s, %s",
                    "STUDENT", "CANDIDATE_TA", "TA", "LECTURER", "ADMIN");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, reason);
        }
    }

    /**
     * A helper method to parse the user role from Jws of Claims to UserRole.
     *
     * @param  claimsJws   the parsed JWS claims
     * @return the user role as UserRole is successful.
     *         If not, ResponseStatusException is thrown.
     */
    private UserRole parseRole(Jws<Claims> claimsJws) {
        try {
            String role = jwtUtils.getRole(claimsJws);
            return UserRole.valueOf(role.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            String reason = String.format("Role must be one of the following: %s, %s, %s, %s, %s",
                    "STUDENT", "CANDIDATE_TA", "TA", "LECTURER", "ADMIN");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, reason);
        }
    }


    /**
     * A helper method to check if a user with the given username (netID) exists.
     *   If not, ResponseStatusException is thrown.
     *   If yes, the User object is returned.
     *
     * @param netId     the netID of the user.
     * @return          the User with the given netId if (s)he exists.
     */
    private User getUserByUsername(String netId) {
        Optional<User> user = this.userService.getUserByNetId(netId);
        if (user.isEmpty()) {
            String reason = String.format("User with NetID %s not found!", netId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, reason);
        }
        return user.get();
    }


    /**
     * A helper method to check if a user with the given user ID exists.
     *   If not, ResponseStatusException is thrown.
     *   If yes, the User object is returned.
     *
     * @param userId    the userId of the user.
     * @return          the User with the given userId if (s)he exists.
     */
    private User getUserByUserId(long userId) {
        Optional<User> user = this.userService.getUserByUserId(userId);
        if (user.isEmpty()) {
            String reason = String.format("User with user ID %s not found!", userId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, reason);
        }
        return user.get();
    }


    /**
     * A helper method to parse and check the validity of a JWT token.
     *
     * @param       jwtPrefixed the prefixed JWT token.
     *                          (after it has been extracted from 'Authorization' header.
     * @return parsed JWS claims if successful.
     *         If not, ResponseStatusException is thrown.
     */
    private Jws<Claims> parseAndValidateJwt(String jwtPrefixed) {
        String jwt = jwtUtils.resolveToken(jwtPrefixed);

        if (jwt == null) {
            String reason = "'Authorization' header must start with 'Bearer '";
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, reason);
        }

        Jws<Claims> claimsJws = jwtUtils.validateAndParseClaims(jwt);
        if (claimsJws == null) {
            String reason = "JWT token is invalid or has been expired";
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, reason);
        }

        return claimsJws;
    }


    /**
     * A helper method to attempt to register a user and deal with exceptions.
     *
     * @param netId     the netID of the user.
     * @param firstName the first name of the user.
     * @param lastName  the last name of the user.
     * @return the created user ID if successful,
     *         if not (user with the same netID already exists),
     *           then ResponseStatusException is thrown.
     */
    private long attemptToRegister(String netId, String firstName, String lastName) {
        try {
            return this.userService.registerUser(netId, firstName, lastName);
        } catch (DataIntegrityViolationException e) {
            String reason = String.format("User with NetID %s already exists!", netId);
            throw new ResponseStatusException(HttpStatus.CONFLICT, reason);
        }
    }
}
