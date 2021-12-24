package nl.tudelft.sem.authentication.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import nl.tudelft.sem.authentication.entities.Notification;
import nl.tudelft.sem.authentication.entities.UserData;
import nl.tudelft.sem.authentication.jwt.JwtTokenProvider;
import nl.tudelft.sem.authentication.security.UserRole;
import nl.tudelft.sem.authentication.service.AuthService;
import nl.tudelft.sem.authentication.service.NotificationService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.DeleteMapping;
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
    // Some constant values we use often.
    private static final transient String USERNAME = "username";
    private static final transient String PASSWORD = "password";
    private static final transient String USERID = "userId";

    private final transient AuthService authService;
    private final transient NotificationService notificationService;
    private final transient JwtTokenProvider jwtTokenProvider;
    private final transient AuthenticationManager authenticationManager;
    private final transient ObjectMapper objectMapper = new ObjectMapper();


    /**
     * Instantiates a new AuthenticationController object.
     *
     * @param authService                the authentication service.
     * @param notificationService        the notification service.
     * @param jwtTokenProvider           the class with JWT utilities.
     * @param authenticationManager      the authentication manager.
     */
    public AuthController(AuthService authService, NotificationService notificationService,
                          JwtTokenProvider jwtTokenProvider,
                          AuthenticationManager authenticationManager) {
        this.authService = authService;
        this.notificationService = notificationService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
    }


    /**
     * Registers a new user to the system, if (s)he is not already registered.
     *
     * @param req the HTTP request.
     * @throws IOException IO exception if something goes wrong with the servlets.
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void register(HttpServletRequest req) throws IOException {
        // Get all the necessary fields from the request body.
        JsonNode jsonNode = objectMapper.readTree(req.getInputStream());
        final String username = jsonNode.get(USERNAME).asText();
        final long userId = Long.parseLong(jsonNode.get(USERID).asText());
        final String password = jsonNode.get(PASSWORD).asText();

        // Try to register a new user, if failed an exception will be thrown.
        if (!this.authService.registerUser(username, userId, password)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    String.format("User with username %s or NetID %s already exists!",
                            username, userId));
        }
    }


    /**
     * Changes the password of a user if the provided credentials are correct.
     *
     * @param req the HTTP request.
     * @throws IOException IO exception if something goes wrong with the servlets.
     */
    @PutMapping("/change_password")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void changePassword(HttpServletRequest req) throws IOException {
        // Get all the necessary fields from the request body.
        JsonNode jsonNode = objectMapper.readTree(req.getInputStream());
        String target = jsonNode.get(USERNAME).asText();
        String jwt = jwtTokenProvider.resolveToken(req);

        // Check if the requester wants to change his own password.
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
    @ResponseBody
    public void login(HttpServletRequest req,
                 HttpServletResponse res) throws IOException {
        // Get all the necessary fields from the request body.
        JsonNode jsonNode = objectMapper.readTree(req.getInputStream());
        String username = jsonNode.get(USERNAME).asText();
        String password = jsonNode.get(PASSWORD).asText();

        try {
            // Authenticate the user if valid credentials are provided.
            authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(username, password));
            UserData user = this.authService.loadUserByUsername(username);
            String jwt = jwtTokenProvider
                    .createToken(user.getUserId(), user.getRole(), new Date());

            // Set the authorization header to contain the corresponding JWT token.
            res.setHeader(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", jwt));
            res.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");

            // Fetch notifications from user, if they exist.
            List<Notification> list = getAllNotificationsFromUser(user.getUserId());
            String json = turnListInJsonResponse(list);
            res.getWriter().write(json);
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid credentials.");
        } catch (EntityNotFoundException e) {
            res.getWriter().write("No new notifications.");
        }
    }

    /**
     * Changes the role of a user, if the user is an admin or lecturer.
     *
     * @param req the HTTP request.
     * @throws IOException IO exception if something goes wrong with the servlets.
     */
    @PutMapping("/change_role")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void changeRole(HttpServletRequest req) throws IOException {
        // Get username to change the role of from the request body.
        JsonNode jsonNode = objectMapper.readTree(req.getInputStream());
        String target = jsonNode.get(USERNAME).asText();

        String newRoleInput = jsonNode.get("role").asText();
        UserRole newRole = getRole(newRoleInput);
        this.authService.changeRole(target, newRole);
    }

    /**
     * Deletes the specified user. Only possible by ADMIN.
     *
     * @param req the HTTP request.
     * @throws IOException IO exception if something goes wrong with the servlets.
     */
    @DeleteMapping("/delete")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void delete(HttpServletRequest req) throws IOException {
        // Get all the necessary fields from the request body.
        JsonNode jsonNode = objectMapper.readTree(req.getInputStream());
        String target = jsonNode.get(USERNAME).asText();
        this.authService.deleteUser(target);
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
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Please enter a valid role.");
        }
    }

    /**
     * Gets all notifications from specific user.
     *
     * @param targetUserId the target user id.
     * @return all notifications from user.
     */
    public List<Notification> getAllNotificationsFromUser(long targetUserId) {
        return this.notificationService.loadNotificationByUserId(targetUserId);
    }

    /**
     * Turn list of notifications in json response string.
     *
     * @param list the list of notifications.
     *
     * <i>Example:</i>
     * <pre>
     *  {
     *      "notifications": [
     *          {
     *             "message" : "Hey there, you are hired!",
     *             "notificationDate" : "17:54 10-12-2021 Europe/Berlin"
     *          }
     *          {
     *              "message": "Hey there, you are fired!",
     *              "notificationDate": "16:20 25-12-2021 Europe/Berlin"
     *          }
     *      ]
     *  }
     * </pre>
     *
     * @return string representation of list of notifications.
     */
    public String turnListInJsonResponse(List<Notification> list) {
        StringBuilder json = new StringBuilder("{\"notifications\":[");
        for (int i = 0; i < list.size(); i++) {
            Notification n = list.get(i);
            json.append(n.toJsonResponse());
            if (i < list.size() - 1) {
                json.append(", ");
            }
        }
        json.append("]}");
        return json.toString();
    }
}
