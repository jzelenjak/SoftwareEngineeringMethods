package nl.tudelft.sem.authentication.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import nl.tudelft.sem.authentication.jwt.JwtUtils;
import nl.tudelft.sem.authentication.security.UserRole;
import nl.tudelft.sem.authentication.service.AuthService;
import org.springframework.dao.DataIntegrityViolationException;
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
    private final transient AuthService authService;
    private final transient ObjectMapper objectMapper;
    private final transient JwtUtils jwtUtils;
    private final transient AuthenticationManager authenticationManager;


    /**
     * Instantiates a new authentication controller.
     *
     * @param authService           the authentication service
     * @param jwtUtils              the JWT utils class
     * @param authenticationManager the authentication manager
     */
    public AuthController(AuthService authService, JwtUtils jwtUtils,
                          AuthenticationManager authenticationManager) {
        this.authService = authService;
        this.jwtUtils = jwtUtils;
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
        final String pwd = jsonNode.get(password).asText();

        if (!this.authService.registerUser(uname, pwd)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    String.format("User with NetID %s already exists!", username));
        }
        return String.format("User with NetID %s successfully registered!", uname);
        // marks response as committed -- if we don't do this the request will go through normally
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
        try {
            JsonNode jsonNode = objectMapper.readTree(req.getInputStream());
            String jwt = jwtUtils.resolveToken(req);
            String newPassword = jsonNode.get("new_password").asText();
            this.authService.changePassword(jwtUtils.getUsername(jwt), newPassword);

            return "Password successfully changed!";
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    String.format("You are not %s and cannot change the credentials.", username));
        }
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

            String jwt = jwtUtils
                    .createToken(uname, this.authService
                            .loadUserByUsername(uname).getRole(), new Date());

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
     * @param req the HTTP request
     * @param res the HTTP response
     * @throws IOException IO exception if something goes wrong with the servlets.
     */
    @PutMapping("/change_role")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    String changeRole(HttpServletRequest req,
                      HttpServletResponse res) throws IOException {
        try {
            String jwt = jwtUtils.resolveToken(req);
            String roleOfRequester = jwtUtils.getRole(jwt).toUpperCase(Locale.ROOT);
            if (!roleOfRequester.equals("ADMIN") || !roleOfRequester.equals("LECTURER")) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        String.format("You are not allowed to do that! %s", jwtUtils.getRole(jwt)));
            }
            UserRole newRole;
            JsonNode jsonNode = objectMapper.readTree(req.getInputStream());
            if (roleOfRequester.equals("LECTURER")) {
                newRole = UserRole.TA;
            } else {
                String newRoleInput = jsonNode.get("role").asText();
                newRole = getRole(newRoleInput);
            }

            this.authService.changeRole(jsonNode.get(username).asText(), newRole);

            return "Role successfully changed!";
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You need to login to do that!");
        }
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
                return null;
        }
    }
}
