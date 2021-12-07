package nl.tudelft.sem.authentication.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import nl.tudelft.sem.authentication.entities.UserData;
import nl.tudelft.sem.authentication.jwt.JwtTokenProvider;
import nl.tudelft.sem.authentication.security.UserRole;
import nl.tudelft.sem.authentication.service.AuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;


/**
 * A controller class for authentication.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final transient String USERNAME = "username";
    private static final transient String PASSWORD = "password";
    private static final transient String USERID = "userId";

    private final transient AuthService authService;

    private final transient JwtTokenProvider jwtTokenProvider;

    private final transient AuthenticationManager authenticationManager;

    private final transient ObjectMapper objectMapper = new ObjectMapper();


    /**
     * Instantiates a new AuthenticationController object.
     *
     * @param authService           the authentication service
     * @param jwtTokenProvider      the class with JWT utilities
     * @param authenticationManager the authentication manager
     */
    public AuthController(AuthService authService, JwtTokenProvider jwtTokenProvider,
                          AuthenticationManager authenticationManager) {
        this.authService = authService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
    }


    /**
     * Registers a new user to the system, if (s)he is not already registered.
     *
     * @param req the HTTP request
     * @param res the HTTP response
     * @throws IOException IO exception if something goes wrong with the servlets.
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    void register(HttpServletRequest req,
                    HttpServletResponse res) throws IOException {
        JsonNode jsonNode = objectMapper.readTree(req.getInputStream());
        final String username = jsonNode.get(USERNAME).asText();
        final long userId = Long.parseLong(jsonNode.get(USERID).asText());
        final String password = jsonNode.get(PASSWORD).asText();

        if (!this.authService.registerUser(username, userId, password)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    String.format("User with NetID %s already exists!", USERNAME));
        }
        // TODO: decide on how do we send back the token
    }


    /**
     * Changes the password of a user if the provided credentials are correct.
     *
     * @param req the HTTP request
     * @param res the HTTP response
     * @throws IOException IO exception if something goes wrong with the servlets.
     */
    @PutMapping("/change_password")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    void changePassword(HttpServletRequest req,
                          HttpServletResponse res) throws IOException {
        JsonNode jsonNode = objectMapper.readTree(req.getInputStream());
        String target = jsonNode.get(USERNAME).asText();
        String jwt = jwtTokenProvider.resolveToken(req);
        if (!target.equals(jwtTokenProvider.getUsername(jwt))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    String.format("You are not %s and are not allowed to change password!",
                            target));
        }
        String newPassword = jsonNode.get("newPassword").asText();
        this.authService.changePassword(target, newPassword);
    }

    /**
     * Logs in the user.
     * If the login has been successful, sends back JWT in 'Authorization' header:
     * e.g 'Authorization' : 'Bearer «token»'
     *
     * @param req the HTTP request.
     * @param res the HTTP response.
     * @throws IOException IO exception if something goes wrong with the servlets.
     */
    @GetMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    void login(HttpServletRequest req,
                 HttpServletResponse res) throws IOException {
            JsonNode jsonNode = objectMapper.readTree(req.getInputStream());
            String username = jsonNode.get(USERNAME).asText();
            String password = jsonNode.get(PASSWORD).asText();
        try {
            authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(username, password));
            UserData user = this.authService.loadUserByUsername(username);
            String jwt = jwtTokenProvider
                    .createToken(user.getUserId(), user.getRole(), new Date());

            res.setHeader(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", jwt));
            res.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid credentials.");
        }
    }

    /**
     * Changes the password of a user if the provided credentials are correct.
     *
     * @param req the HTTP request.
     * @param res the HTTP response.
     * @throws IOException IO exception if something goes wrong with the servlets.
     */
    @PutMapping("/change_role")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    void changeRole(HttpServletRequest req,
                      HttpServletResponse res) throws IOException {
        // Get JWT from the requester.
        String jwt = jwtTokenProvider.resolveToken(req);
        Jws<Claims> claimsJws = jwtTokenProvider.validateAndParseToken(jwt);
        String roleOfRequester = jwtTokenProvider.getRole(claimsJws);

        // Check if requester is a lecturer.
        JsonNode jsonNode = objectMapper.readTree(req.getInputStream());
        String target = jsonNode.get(USERNAME).asText();
        if (getRole(roleOfRequester) == UserRole.LECTURER) {
            // Lecturer can only change a student's role to TA.
            if (this.authService.loadUserByUsername(target).getRole() != UserRole.STUDENT) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "You are not allowed to do that as a lecturer!");
            }
            // Lecturer can only change role to TA
            String newRoleInput = jsonNode.get("role").asText();
            UserRole newRole = getRole(newRoleInput);
            if (newRole != UserRole.TA) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "You are not allowed to do that as a lecturer!");
            }

        }
        String newRoleInput = jsonNode.get("role").asText();
        UserRole newRole = getRole(newRoleInput);
        this.authService.changeRole(target, newRole);
    }

    /**
     * Gets role for a given string.
     *
     * @param newRoleFromInput the new role from input.
     * @return role for the given string as enum element.
     */
    public UserRole getRole(String newRoleFromInput) {
        switch (newRoleFromInput.toUpperCase(Locale.ROOT)) {
            case "ADMIN":
                return UserRole.ADMIN;
            case "STUDENT":
                return UserRole.STUDENT;
            case "LECTURER":
                return UserRole.LECTURER;
            case "TA":
                return UserRole.TA;
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Please enter a valid role.");
        }
    }
}
