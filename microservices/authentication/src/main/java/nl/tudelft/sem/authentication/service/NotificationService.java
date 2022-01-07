package nl.tudelft.sem.authentication.service;

import java.util.List;
import javax.persistence.EntityNotFoundException;
import nl.tudelft.sem.authentication.entities.Notification;
import nl.tudelft.sem.authentication.repositories.NotificationRepository;
import org.springframework.stereotype.Service;


/**
 * A class that represents Service.
 * Which communicates with the database containing the notification details.
 */
@Service
public class NotificationService {

    private final transient NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /**
     * Add new notification.
     *
     * @param userId         the user id of the notification.
     * @param message        the message of the notification.
     *
     * @return true when successfully added.
     */
    public boolean addNewNotification(long userId, String message) {
        this.notificationRepository
                .save(new Notification(userId, message));
        return true;
    }

    /**
     * Change user from notification.
     *
     * @param notificationId the notification id from the notification.
     * @param userId         the user id to change the notification belonging to.
     */
    public void changeUserIdFromNotification(long notificationId, long userId) {
        Notification notification = loadNotificationByNotificationId(notificationId);
        notification.setUserId(userId);
        notificationRepository.save(notification);
    }

    /**
     * Change message from notification.
     *
     * @param notificationId the notification id
     * @param message        the message
     */
    public void changeMessageFromNotification(long notificationId, String message) {
        Notification notification = loadNotificationByNotificationId(notificationId);
        notification.setMessage(message);
        notificationRepository.save(notification);
    }

    /**
     * Load notification by notification id of the notification.
     *
     * @param notificationId the notification id.
     * @return notification if found, else throw exception.
     */
    public Notification loadNotificationByNotificationId(long notificationId) {
        return this.notificationRepository
                .findByNotificationId(notificationId)
                .orElseThrow(() -> new EntityNotFoundException(String.format(
                        "Notification with id %d not found", notificationId)));
    }

    /**
     * Load notification by user id list.
     *
     * @param userId the user id to search notifications from.
     *
     * @return the list of notifications from the user if present, else throw exception.
     */
    public List<Notification> loadNotificationByUserId(long userId) {
        return this.notificationRepository
                .findByUserId(userId)
                .orElseThrow(() ->
                        new EntityNotFoundException(String.format(
                                "Notification with user ID %d has no new notification.",
                                        userId)));
    }

    /**
     * Delete notification by notification id.
     *
     * @param notificationId the notification id of the notification.
     */
    public void deleteNotificationByNotificationId(long notificationId) {
        Notification notification = loadNotificationByNotificationId(notificationId);
        notificationRepository.delete(notification);
    }

    /**
     * Delete notifications from user specified by their userId.
     *
     * @param userId the user id of the user to delete the notifications from.
     */
    public void deleteNotificationsFromUser(long userId) {
        List<Notification> notifications = loadNotificationByUserId(userId);
        notificationRepository.deleteAll(notifications);
    }
}
