package nl.tudelft.sem.authentication.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final transient String username = "username";
    private final transient String password = "password";
    private final transient String userIdStr = "userId";
    private final transient AuthService authService;
    private final transient ObjectMapper objectMapper;
    private final transient JwtTokenProvider jwtTokenProvider;
    private final transient AuthenticationManager authenticationManager;


    /**
     * Instantiates a new authentication controller.
     *
     * @param authService           the authentication service
     * @param jwtTokenProvider              the JWT utils class
     * @param authenticationManager the authentication manager
     */
    public AuthController(AuthService authService, JwtTokenProvider jwtTokenProvider,
                          AuthenticationManager authenticationManager) {
        this.authService = authService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
        this.objectMapper = new ObjectMapper();
    }


    /**
     * Registers a new user to the system, if not (s)he is not already registered.
     *
     * @param req the HTTP request
     * @param res the HTTP response
     * @throws IOException IO exception if something goes wrong with the servlets.
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    String register(HttpServletRequest req,
                    HttpServletResponse res) throws IOException {
        JsonNode jsonNode = objectMapper.readTree(req.getInputStream());
        final String uname = jsonNode.get(username).asText();
        final long userId = Long.parseLong(jsonNode.get(userIdStr).asText());
        final String pwd = jsonNode.get(password).asText();

        if (!this.authService.registerUser(uname, userId, pwd)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    String.format("User with NetID %s already exists!", username));
        }
        return String.format("User with NetID %s successfully registered!", uname);
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
    String changePassword(HttpServletRequest req,
                          HttpServletResponse res) throws IOException {
        String jwt = jwtTokenProvider.resolveToken(req);
        if (jwt == null || jwt.equals("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "You need to login to change your password!");
        }
        JsonNode jsonNode = objectMapper.readTree(req.getInputStream());
        String target = jsonNode.get(username).asText();
        if (!target.equals(jwtTokenProvider.getUsername(jwt))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    String.format("You are not %s and are not allowed to change password!",
                            target));
        }
        String newPassword = jsonNode.get("newPassword").asText();
        this.authService.changePassword(target, newPassword);

        return "Password successfully changed!";
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
    String login(HttpServletRequest req,
                 HttpServletResponse res) throws IOException {
        try {
            JsonNode jsonNode = objectMapper.readTree(req.getInputStream());
            String uname = jsonNode.get(username).asText();
            String pwd = jsonNode.get(password).asText();

            authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(uname, pwd));
            UserData user = this.authService.loadUserByUsername(uname);
            String jwt = jwtTokenProvider
                    .createToken(user.getUserId(), user.getRole(), new Date());

            String jwtPrefixed = String.format("Bearer %s", jwt);
            res.setHeader(HttpHeaders.AUTHORIZATION, jwtPrefixed);
            res.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
            return "Login successful!";
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Invalid credentials");
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
    String changeRole(HttpServletRequest req,
                      HttpServletResponse res) throws IOException {
        // Get JWT from the requester.
        String jwt = jwtTokenProvider.resolveToken(req);
        String roleOfRequester = jwtTokenProvider.getRole(jwt);

        // Check if requester is a lecturer.
        JsonNode jsonNode = objectMapper.readTree(req.getInputStream());
        String target = jsonNode.get(username).asText();
        if (getRole(roleOfRequester) == UserRole.LECTURER) {
            // Lecturer can only change a student's role to TA.
            if (authService.loadUserByUsername(target).getRole() != UserRole.STUDENT) {
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

        return "Role successfully changed!";

    }

    /**
     * Gets role for a given string.
     *
     * @param newRoleInput the new role input.
     * @return role for the given string as enum.
     */
    public UserRole getRole(String newRoleInput) {
        switch (newRoleInput.toUpperCase(Locale.ROOT)) {
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
