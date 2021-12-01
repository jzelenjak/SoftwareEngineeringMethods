package nl.tudelft.sem.authentication.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.tudelft.sem.authentication.service.AuthService;
import nl.tudelft.sem.authentication.jwt.JwtUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@RestController
@RequestMapping("/auth")
public class AuthController {
    private final transient String USERNAME = "username";
    private final transient String PASSWORD = "password";
    private final transient AuthService authService;
    private final transient ObjectMapper objectMapper = new ObjectMapper();
    private final transient JwtUtils jwtTokenProvider;
    private final transient AuthenticationManager authenticationManager;

    /**
     * Instantiates Authentication controller.
     *
     * @param authService     the authentication service.
     * @param jwtTokenProvider JWT token provider that generates JTW tokens
     * @param authenticationManager the authentication manager
     */
    public AuthController(AuthService authService, JwtUtils jwtTokenProvider,
                          AuthenticationManager authenticationManager) {
        this.authService = authService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
    }

    //Registers a new user to the system, if not already.
    @PostMapping("/register")
    public void register(HttpServletRequest req, HttpServletResponse res) throws IOException {
        JsonNode jsonNode = objectMapper.readTree(req.getInputStream());
        final String username = jsonNode.get(USERNAME).asText();
        final String password = jsonNode.get(PASSWORD).asText();

        res.resetBuffer();
        if (!this.authService.registerUser(username, password)) {
            res.setStatus(HttpServletResponse.SC_CONFLICT);
            res.getOutputStream()
                    .print(String.format("{\"message\":\"User with netid %s already exists!\"}", username));
        } else {
            res.setStatus(HttpServletResponse.SC_OK);
            res.getOutputStream()
                    .print(String.format("{\"message\":\"User with netid %s successfully registered!\"}", username));
        }
        res.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        res.flushBuffer(); // marks response as committed -- if we don't do this the request will go through normally!
    }


    /**
     * Allows the user to change their own credentials if authorized.
     *
     */
    @PutMapping("/change_password")
    public void changePassword(HttpServletRequest req, HttpServletResponse res) throws IOException {
        JsonNode jsonNode = objectMapper.readTree(req.getInputStream());
        String username = jsonNode.get(USERNAME).asText();
        String password = jsonNode.get(PASSWORD).asText();

        authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(username, password));

        String newPassword = jsonNode.get("new_password").asText();
        this.authService.changePassword(username, newPassword);

        res.setStatus(HttpServletResponse.SC_OK);
        res.getOutputStream().print("{\"message\":\"Password successfully changed!\"}");
        res.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        res.flushBuffer();
    }


    @GetMapping("/login")
    public void login(HttpServletRequest req, HttpServletResponse res) throws IOException {
        JsonNode jsonNode = objectMapper.readTree(req.getInputStream());
        String username = jsonNode.get(USERNAME).asText();
        String password = jsonNode.get("password").asText();

        authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(username, password));

        String jwt = jwtTokenProvider.createToken(username, this.authService.loadUserByUsername(username).getRole());
        String jwtPrefixed = String.format("Bearer %s", jwt);

        res.setStatus(HttpServletResponse.SC_OK);
        res.setHeader(HttpHeaders.AUTHORIZATION, jwtPrefixed);
        res.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        res.flushBuffer();
    }
}
