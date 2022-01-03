package nl.tudelft.sem.authentication.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import nl.tudelft.sem.authentication.entities.Notification;
import nl.tudelft.sem.authentication.jwt.JwtTokenProvider;
import nl.tudelft.sem.authentication.security.UserRole;
import nl.tudelft.sem.authentication.service.NotificationService;
import nl.tudelft.sem.jwt.JwtUtils;
import org.springframework.http.HttpStatus;
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
 * A controller class for notifications.
 */
@RestController
@RequestMapping("/api/auth/notifications")
public class NotificationController {
    private static final transient String NOTIFICATIONID = "notificationId";
    private static final transient String USERID = "userId";
    private static final transient String MESSAGE = "message";
    private static final transient String NEWUSER = "newUser";
    private static final transient String NEWMESSAGE = "newMessage";

    private final transient NotificationService notificationService;
    private final transient JwtTokenProvider jwtTokenProvider;
    private final transient JwtUtils jwtUtils;
    private final transient ObjectMapper objectMapper = new ObjectMapper();


    /**
     * Instantiates a new Notification controller.
     *
     * @param notificationService   the notification service.
     * @param jwtTokenProvider      the jwt token provider.
     * @param jwtUtils              the jwt utils library.
     */
    public NotificationController(NotificationService notificationService,
                                  JwtTokenProvider jwtTokenProvider, JwtUtils jwtUtils) {
        this.notificationService = notificationService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtUtils = jwtUtils;
    }

    /**
     * Gets all notifications from specified user. Only possible by admin.
     *
     * @param req the HTTP request.
     *
     * @return all notifications from current user.
     */
    @GetMapping("/get")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<Notification> getAllNotificationsFromUser(HttpServletRequest req)
            throws IOException {
        JsonNode jsonNode = objectMapper.readTree(req.getInputStream());
        final long targetUserId = Long.parseLong(jsonNode.get(USERID).asText());

        // Only admin can do that, so check this.
        checkAdmin(req);

        return this.notificationService.loadNotificationByUserId(targetUserId);
    }

    /**
     * Add new notification. Only possible by admin.
     * If not, ResponseStatusException with 403 FORBIDDEN will be thrown.
     *
     * @param req the HTTP request.
     * @throws IOException IO exception if something goes wrong with the servlets.
     */
    @PostMapping("/add")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void addNewNotification(HttpServletRequest req) throws IOException {
        // Only admin or lecturer is allowed to do this, so we check this.
        checkAdminOrLecturer(req);

        JsonNode jsonNode = objectMapper.readTree(req.getInputStream());
        final long userId = Long.parseLong(jsonNode.get(USERID).asText());
        final String message = jsonNode.get(MESSAGE).asText();

        // Create new notification.
        this.notificationService.addNewNotification(userId, message);
    }

    /**
     * Change user from notification. Only possible by admin.
     * If not, ResponseStatusException with 403 FORBIDDEN will be thrown.
     *
     * @param req the HTTP request.
     * @throws IOException IO exception if something goes wrong with the servlets.
     */
    @PutMapping("/change_user")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void changeUserFromNotification(HttpServletRequest req) throws IOException {
        // Only admin is allowed to do this, so we check this.
        checkAdmin(req);

        JsonNode jsonNode = objectMapper.readTree(req.getInputStream());
        final long notification = Long.parseLong(jsonNode.get(NOTIFICATIONID).asText());
        final long newUser = Long.parseLong(jsonNode.get(NEWUSER).asText());
        this.notificationService.changeUserIdFromNotification(notification, newUser);
    }

    /**
     * Change message from notification. Only possible by admin.
     * If not, ResponseStatusException with 403 FORBIDDEN will be thrown.
     *
     * @param req the HTTP request.
     * @throws IOException the io exception if something goes wrong with the servlets.
     */
    @PutMapping("/change_message")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void changeMessageFromNotification(HttpServletRequest req) throws IOException {
        // Only admin is allowed to do this, so we check this.
        checkAdmin(req);

        JsonNode jsonNode = objectMapper.readTree(req.getInputStream());
        final long notification = Long.parseLong(jsonNode.get(NOTIFICATIONID).asText());
        final String newMessage = jsonNode.get(NEWMESSAGE).asText();
        this.notificationService.changeMessageFromNotification(notification, newMessage);
    }

    /**
     * Deletes the notification specified by id. Only possible by admin.
     * If not, ResponseStatusException with 403 FORBIDDEN will be thrown.
     *
     * @param req the HTTP request.
     * @throws IOException IO exception if something goes wrong with the servlets.
     */
    @DeleteMapping("/delete")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void deleteNotificationById(HttpServletRequest req) throws IOException {
        // Only admin is allowed to do this, so we check this.
        checkAdmin(req);

        JsonNode jsonNode = objectMapper.readTree(req.getInputStream());
        final long notification = Long.parseLong(jsonNode.get(NOTIFICATIONID).asText());
        this.notificationService.deleteNotificationByNotificationId(notification);
    }

    /**
     * Delete notification from user. Only possible by admin.
     * If not, ResponseStatusException with 403 FORBIDDEN will be thrown.
     *
     * @param req the HTTP request.
     * @throws IOException the io exception if something goes wrong with the servlets.
     */
    @DeleteMapping("/delete_user")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void deleteNotificationFromUser(HttpServletRequest req) throws IOException {
        // Only admin is allowed to do this, so we check this.
        checkAdmin(req);

        JsonNode jsonNode = objectMapper.readTree(req.getInputStream());
        final long user = Long.parseLong(jsonNode.get(USERID).asText());
        this.notificationService.deleteNotificationsFromUser(user);
    }

    /**
     * Checks whether the user from the request is an admin or not.
     * If not, ResponseStatusException with 403 FORBIDDEN will be thrown.
     *
     * @param req the HTTP request.
     */
    private void checkAdmin(HttpServletRequest req) {
        // Get info from the user from the provided jwt token (claims).
        String jwt = jwtTokenProvider.resolveToken(req);
        Jws<Claims> claimsJws = jwtTokenProvider.validateAndParseToken(jwt);

        // Check if user is an admin, if not throw an exception.
        if (!isAdmin(claimsJws)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You are not allowed to do that!");
        }
    }

    /**
     * Checks whether the user from the request is an admin/lecturer or not.
     * If not, ResponseStatusException with 403 FORBIDDEN will be thrown.
     *
     * @param req the HTTP request.
     */
    private void checkAdminOrLecturer(HttpServletRequest req) {
        // Get info from the user from the provided jwt token (claims).
        String jwt = jwtTokenProvider.resolveToken(req);
        Jws<Claims> claimsJws = jwtTokenProvider.validateAndParseToken(jwt);
        UserRole role = UserRole.valueOf(jwtTokenProvider.getRole(claimsJws));

        if (!isAdmin(claimsJws) && role != UserRole.LECTURER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You are not allowed to do that!");
        }
    }

    /**
     * Checks whether a certain user is admin or not, based on the claims.
     *
     * @param claimsJws the claims from a parsed token.
     * @return true if admin, false otherwise.
     */
    private boolean isAdmin(Jws<Claims> claimsJws) {
        UserRole role = UserRole.valueOf(jwtTokenProvider.getRole(claimsJws));
        return role == UserRole.ADMIN;
    }
}
