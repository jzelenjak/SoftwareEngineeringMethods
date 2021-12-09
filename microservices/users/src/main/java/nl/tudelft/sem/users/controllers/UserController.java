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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import nl.tudelft.sem.jwt.JwtUtils;
import nl.tudelft.sem.users.config.GatewayConfig;
import nl.tudelft.sem.users.entities.User;
import nl.tudelft.sem.users.entities.UserRole;
import nl.tudelft.sem.users.services.UserService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
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

    private final transient ObjectMapper mapper;

    private final transient JwtUtils jwtUtils;

    private final transient WebClient webClient;

    private final transient GatewayConfig gatewayConfig;

    private static final transient String USERID = "userId";
    private static final transient String ROLE = "role";


    /**
     * Instantiates a new User controller object.
     *
     * @param userService the user service object
     * @param jwtUtils    the utilities for JWT
     */
    public UserController(UserService userService, JwtUtils jwtUtils, GatewayConfig gatewayConfig) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;

        this.mapper = new ObjectMapper();
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
     * @param res   the HTTP response
     * @return the user ID of a new registered user
     *           if there is no user with the same username (netID) already.
     *         In addition, in case of success the JWT token is sent in the
     *           'Authorization' header in the HTTP response.
     *
     *           If the user with the provided netID already exists in the database,
     *             then 409 CONFLICT status is sent back.
     */
    @PostMapping("/register")
    public @ResponseBody Mono<ResponseEntity<String>> registerUser(HttpServletRequest req, HttpServletResponse res) {
        JsonNode jsonNode = getJsonNode(req);
        String username = parseJsonField(jsonNode, "username");
        String firstName = parseJsonField(jsonNode, "firstName");
        String lastName = parseJsonField(jsonNode, "lastName");
        String password = parseJsonField(jsonNode, "password");

        long userId = attemptToRegister(username, firstName, lastName);

        return webClient
                .post()
                .uri(buildUri(gatewayConfig.getHost(), gatewayConfig.getPort(), "api", "auth", "register"))
                .body(Mono
                    .just(createJson("username", username, USERID, String.valueOf(userId),
                            "password", password)), String.class)
                .exchange()
                .flatMap(response -> {
                    if (response.statusCode().isError()) {
                        // Fail the registration if the user could not be registered in Auth Server
                        this.userService.deleteUserByUserId(userId, UserRole.ADMIN);
                        return Mono
                            .error(new ResponseStatusException(response.statusCode(), "Registration failed!"));
                    }

                    return Mono
                        .just(new ResponseEntity<>(createJson(USERID, String.valueOf(userId)),
                                response.headers().asHttpHeaders(), HttpStatus.OK));
                });
    }


    /**
     * Gets a user by their username (netID).
     *
     * @param req   the HTTP request
     * @param res   the HTTP response
     * @return the user with the given username if they exist.
     *         If the user with the provided netID does not exist in the database,
     *           then 404 NOT FOUND status is sent back.
     * @throws IOException when something goes wrong with servlets
     */
    @GetMapping("/by_username")
    public @ResponseBody
    String getByUsername(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String username = parseJsonField(getJsonNode(req), "username");
        return mapper.writeValueAsString(getUserByUsername(username));
    }


    /**
     * Gets a user by their user ID.
     *
     * @param req   the HTTP request
     * @param res   the HTTP response
     * @return the user with the given user ID if they exist.
     *         If the user does not exist, then 404 NOT FOUND status is sent.
     *         If the provided user ID is not a number, then 400 BAD REQUEST is sent back.
     * @throws IOException when something goes wrong with servlets
     */
    @GetMapping("/by_userid")
    public @ResponseBody
    String getByUserId(HttpServletRequest req, HttpServletResponse res) throws IOException {
        long userId = parseUserId(parseJsonField(getJsonNode(req), USERID));
        return mapper.writeValueAsString(getUserByUserId(userId));
    }

    /**
     * Gets users by the role.
     *
     * @param req   the HTTP request
     * @param res   the HTTP response
     * @return the users with the given role.
     *         If no users are found, then 404 NOT FOUND is sent back.
     * @throws IOException when something goes wrong with servlets
     */
    @GetMapping("/by_role")
    public @ResponseBody
    String getByRole(HttpServletRequest req, HttpServletResponse res) throws IOException {
        UserRole role = parseRole(parseJsonField(getJsonNode(req), ROLE).toUpperCase(Locale.ROOT));

        List<User> users = this.userService.getUsersByRole(role);
        if (users.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    String.format("No users having role '%s' are found!", role));
        }
        return mapper.writeValueAsString(users);
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
     * @param res   the HTTP response
     */
    @PutMapping("/change_role")
    public void changeRole(HttpServletRequest req, HttpServletResponse res) {
        JsonNode jsonNode = getJsonNode(req);

        long userId = parseUserId(parseJsonField(jsonNode, USERID));
        UserRole newRole =
                parseRole(parseJsonField(jsonNode, ROLE).toUpperCase(Locale.US));

        Jws<Claims> claimsJws = parseAndValidateJwt(req.getHeader(HttpHeaders.AUTHORIZATION));
        UserRole requesterRole = parseRole(claimsJws);

        if (!this.userService.changeRole(userId, newRole, requesterRole)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Operation not allowed!");
        }

        // TODO: send a request to the Authentication server
//        return webClient
//                .put()
//                .uri(buildUri(gatewayConfig.getHost(), gatewayConfig.getPort(), "api", "auth", "change_role"))
//                .body(Mono.just(createJson("username", u)))
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
     * @param res   the HTTP response
     */
    @DeleteMapping("/delete")
    public void deleteByUserId(HttpServletRequest req,
                                HttpServletResponse res) {
        long userId = parseUserId(parseJsonField(getJsonNode(req), USERID));

        Jws<Claims> claimsJws = parseAndValidateJwt(req.getHeader(HttpHeaders.AUTHORIZATION));
        UserRole requesterRole = parseRole(claimsJws);

        getUserByUserId(userId);

        if (!this.userService.deleteUserByUserId(userId, requesterRole)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Operation not allowed!");
        }
    }


    /*
     * Helper methods to reduce code duplication.
     */

    /**
     * A helper method to create a URI for HTTP request.
     *
     * @param host      the host in the url
     * @param port      the port in the url
     * @param path      the path in the url
     * @return          the complete String url
     */
    private String buildUri(String host, int port, String... path) {
        return UriComponentsBuilder.newInstance()
                .scheme("http")
                .host(host)
                .port(port)
                .pathSegment(path)
                .toUriString();
    }

    /**
     * A helper method to create json string out of key-value pairs.
     *
     * @param kvPairs       list of key-values, must be an even number
     * @return the json string that can be used in the response body
     */
    private String createJson(String... kvPairs) {
        ObjectNode node = mapper.createObjectNode();

        for (int i = 0; i < kvPairs.length; i += 2) {
            node.put(kvPairs[i], kvPairs[i + 1]);
        }
        return node.toString();
    }

    /**
     * A helper method to get jsonNode from HTTP request input stream
     *   and handle potential exceptions.
     *
     * @param req   HTTP request.
     * @return jsonNode if successful, otherwise ResponseStatusException is thrown.
     */
    private JsonNode getJsonNode(HttpServletRequest req) {
        try {
            return mapper.readTree(req.getInputStream());
        } catch (IOException e) {
            String reason = "Error with servlets while parsing the input";
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, reason);
        }
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
            String reason =
                "Error while parsing JSON. The body is corrupted or required fields are missing";
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
        } catch (NumberFormatException e) {
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
        } catch (IllegalArgumentException e) {
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, reason);
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
