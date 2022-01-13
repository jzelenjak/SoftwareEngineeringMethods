package nl.tudelft.sem.users.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import nl.tudelft.sem.jwt.JwtUtils;
import nl.tudelft.sem.users.config.GatewayConfig;
import nl.tudelft.sem.users.entities.UserRole;
import nl.tudelft.sem.users.services.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

/**
 * Controller class for users that provides `update` API.
 */
@RestController
@RequestMapping("/api/users")
public class UserPersonalInfoController extends UserBaseController {

    /**
     * Instantiates a new User controller object.
     *
     * @param userService   the user service object
     * @param jwtUtils      the utilities for JWT
     * @param gatewayConfig the configuration for the gateway
     */
    public UserPersonalInfoController(UserService userService, JwtUtils jwtUtils,
                                      GatewayConfig gatewayConfig) {
        super(userService, jwtUtils, gatewayConfig);
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
        validate(prefixedJwt, Set.of(UserRole.ADMIN.name()));

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(req.getInputStream());
        long userId = parseUserId(parseJsonField(jsonNode, USERID));
        UserRole newRole = parseRole(parseJsonField(jsonNode, ROLE).toUpperCase(Locale.US));

        String username = this.getUserByUserId(userId).getUsername();
        String json = mapper.createObjectNode().put(USERNAME, username)
                                .put(ROLE, newRole.name()).toString();
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
                String responseJson = mapper.createObjectNode()
                    .put("message", "Changed the role successfully!").toString();
                return Mono.just(ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(responseJson));
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
        validate(req.getHeader(HttpHeaders.AUTHORIZATION), Set.of(UserRole.ADMIN.name()));

        JsonNode jsonNode = new ObjectMapper().readTree(req.getInputStream());
        long userId = parseUserId(parseJsonField(jsonNode, USERID));
        String newFirstName = parseJsonField(jsonNode, FIRSTNAME);
        if (!this.userService.changeFirstName(userId, newFirstName)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                String.format("No user with userid '%d' found!", userId));
        }
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
        validate(req.getHeader(HttpHeaders.AUTHORIZATION), Set.of(UserRole.ADMIN.name()));

        JsonNode jsonNode = new ObjectMapper().readTree(req.getInputStream());
        long userId = parseUserId(parseJsonField(jsonNode, USERID));
        String newLastName = parseJsonField(jsonNode, LASTNAME);
        if (!this.userService.changeLastName(userId, newLastName)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                String.format("No user with userid '%d' found!", userId));
        }
    }
}
