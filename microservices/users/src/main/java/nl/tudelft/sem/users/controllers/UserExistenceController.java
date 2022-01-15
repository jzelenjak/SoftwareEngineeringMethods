package nl.tudelft.sem.users.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import nl.tudelft.sem.jwt.JwtUtils;
import nl.tudelft.sem.users.config.GatewayConfig;
import nl.tudelft.sem.users.entities.UserRole;
import nl.tudelft.sem.users.services.UserService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

/**
 * Controller class for users that provides `create` and `delete` API.
 */
@RestController
@RequestMapping("/api/users")
public class UserExistenceController extends UserBaseController {
    /**
     * Instantiates a new User controller object.
     *
     * @param userService   the user service object
     * @param jwtUtils      the utilities for JWT
     * @param gatewayConfig the configuration for the gateway
     */
    public UserExistenceController(UserService userService, JwtUtils jwtUtils,
                                   GatewayConfig gatewayConfig) {
        super(userService, jwtUtils, gatewayConfig);
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

        long userId = this.userService.registerUser(username, firstName, lastName);
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
        validate(prefixedJwt, Set.of(UserRole.ADMIN.name()));

        long userId = parseUserId(req.getParameter(USERID));
        String username = getUserByUserId(userId).getUsername();
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.createObjectNode().put(USERNAME, username).toString();
        return webClient
            .method(HttpMethod.DELETE)
            .uri(buildUri("api", "auth", "delete"))
            .header(HttpHeaders.CONTENT_TYPE, String.valueOf(MediaType.APPLICATION_JSON))
            .header(HttpHeaders.AUTHORIZATION, prefixedJwt)
            .body(Mono.just(json), String.class)
            .exchange()
            .flatMap(res -> {
                if (res.statusCode().isError()) {
                    // Do not do anything locally to provide consistency
                    return Mono.error(new ResponseStatusException(res.statusCode(),
                        "Could not delete the user!"));
                }
                // Since operation is successful in Auth server, delete the user locally as well
                this.userService.deleteUserByUserId(userId);
                String responseJson = mapper.createObjectNode()
                    .put("message", "User deleted successfully!").toString();
                return Mono.just(ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(responseJson));
            });
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    private ResponseEntity<Object> cannotRegister(DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }
}
