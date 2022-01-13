package nl.tudelft.sem.users.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import nl.tudelft.sem.jwt.JwtUtils;
import nl.tudelft.sem.users.config.GatewayConfig;
import nl.tudelft.sem.users.entities.User;
import nl.tudelft.sem.users.entities.UserRole;
import nl.tudelft.sem.users.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Base Controller class for users. Parent class for different UserControllers.
 */
public abstract class UserBaseController {

    protected final transient UserService userService;

    protected final transient JwtUtils jwtUtils;

    protected final transient WebClient webClient;

    protected final transient GatewayConfig gatewayConfig;

    protected static final transient String USERNAME = "username";
    protected static final transient String FIRSTNAME = "firstName";
    protected static final transient String LASTNAME = "lastName";
    protected static final transient String USERID = "userId";
    protected static final transient String ROLE = "role";


    /**
     * Instantiates a new User controller object.
     *
     * @param userService       the user service object
     * @param jwtUtils          the utilities for JWT
     * @param gatewayConfig     the configuration for the gateway
     */
    public UserBaseController(UserService userService, JwtUtils jwtUtils,
                              GatewayConfig gatewayConfig) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
        this.webClient = WebClient.create();
        this.gatewayConfig = gatewayConfig;
    }

    /**
     * A helper method to create a URI for HTTP request.
     *
     * @param path      the path in the url
     * @return the complete String url
     */
    protected String buildUri(String... path) {
        return UriComponentsBuilder.newInstance().scheme("http")
                .host(gatewayConfig.getHost()).port(gatewayConfig.getPort())
                .pathSegment(path).toUriString();
    }

    /**
     * A helper method to extract the value from a JSON object field
     *   and handle potential exceptions.
     *
     * @param jsonNode  jsonNode with JSON.
     * @param field     the field whose value is to be extracted.
     * @return the extracted value if successful. If not, ResponseStatusException is thrown.
     */
    protected String parseJsonField(JsonNode jsonNode, String field) {
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
    protected long parseUserId(String userIdStr) {
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
    protected UserRole parseRole(String roleStr) {
        try {
            return UserRole.valueOf(roleStr);
        } catch (Exception e) {
            // Either IllegalArgumentException or NullPointerException
            String reason = String.format("Role must be one of the following: %s.",
                    Arrays.stream(UserRole.values()).map(Enum::name)
                            .collect(Collectors.joining(", ")));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, reason);
        }
    }

    /**
     * A helper method to validate the requester using JWT token.
     *
     * @param jwtPrefixed   the JWT token extracted from 'Authorization' header
     * @param allowedRoles  the roles that the token can contain to perform the operation
     */
    protected void validate(String jwtPrefixed, Set<String> allowedRoles) {
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
     * A helper method to get a user with the given user ID if (s)he exists.
     *
     * @param userId the user ID of the user.
     * @return the User with the given user ID if (s)he exists.
     *         If not, ResponseStatusException is thrown
     */
    protected User getUserByUserId(long userId) {
        Optional<User> user = this.userService.getUserByUserId(userId);
        if (user.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                String.format("User with user ID %s not found!", userId));
        }
        return user.get();
    }
}
