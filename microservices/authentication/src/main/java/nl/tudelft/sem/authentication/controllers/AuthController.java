package nl.tudelft.sem.authentication.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import nl.tudelft.sem.authentication.jwt.JwtUtils;
import nl.tudelft.sem.authentication.service.AuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


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
    public void register(HttpServletRequest req, HttpServletResponse res) throws IOException {
        JsonNode jsonNode = objectMapper.readTree(req.getInputStream());
        final String uname = jsonNode.get(username).asText();
        final String pwd = jsonNode.get(password).asText();

        res.resetBuffer();
        if (!this.authService.registerUser(uname, pwd)) {
            res.setStatus(HttpServletResponse.SC_CONFLICT);
            res
                .getOutputStream()
                .print(String
                        .format("{\"message\":\"User with NetID %s already exists!\"}",
                                uname));
        } else {
            res.setStatus(HttpServletResponse.SC_OK);
            res
                .getOutputStream()
                .print(String
                        .format("{\"message\":\"User with NetID %s successfully registered!\"}",
                                uname));
        }
        res.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        res.flushBuffer();
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
    public void changePassword(HttpServletRequest req, HttpServletResponse res) throws IOException {
        JsonNode jsonNode = objectMapper.readTree(req.getInputStream());
        String uname = jsonNode.get(username).asText();
        String pwd = jsonNode.get(password).asText();

        authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(uname, pwd));

        String newPassword = jsonNode.get("new_password").asText();
        this.authService.changePassword(uname, newPassword);

        res.setStatus(HttpServletResponse.SC_OK);
        res.getOutputStream().print("{\"message\":\"Password successfully changed!\"}");
        res.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        res.flushBuffer();
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
    public void login(HttpServletRequest req, HttpServletResponse res) throws IOException {
        JsonNode jsonNode = objectMapper.readTree(req.getInputStream());
        String uname = jsonNode.get(username).asText();
        String pwd = jsonNode.get(password).asText();

        authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(uname, pwd));

        String jwt = jwtUtils
                .createToken(uname, this.authService
                                        .loadUserByUsername(uname).getRole(), new Date());

        String jwtPrefixed = String.format("Bearer %s", jwt);

        res.setStatus(HttpServletResponse.SC_OK);
        res.setHeader(HttpHeaders.AUTHORIZATION, jwtPrefixed);
        res.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        res.getOutputStream().print("{\"message\":\"Login successful!\"}");
        res.flushBuffer();
    }
}
