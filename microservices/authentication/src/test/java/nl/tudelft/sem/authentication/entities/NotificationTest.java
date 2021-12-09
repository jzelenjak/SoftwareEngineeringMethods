package nl.tudelft.sem.authentication.entities;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NotificationTest {
    private transient Notification notification;
    private final transient long notificationId = 42L;
    private final transient long userId = 1234567L;
    private final transient String message = "Hey there, you are hired!";
    private final transient String newMessage = "Unfortunately you are fired, get out.";

    @BeforeEach
    void setUp() {
        notification = new Notification(notificationId, userId, message);
    }

    @Test
    void getNotificationIdTest() {
        Assertions.assertEquals(notificationId, notification.getNotificationId());
    }

    @Test
    void setNotificationIdTest() {
        Notification notification = new Notification();
        notification.setNotificationId(12L);
        Assertions.assertEquals(12L, notification.getNotificationId());
    }

    @Test
    void getUserIdTest() {
        Assertions.assertEquals(userId, notification.getUserId());
    }

    @Test
    void setUserIdTest() {
        Notification notification = new Notification();
        notification.setUserId(userId);
        Assertions.assertEquals(userId, notification.getUserId());
    }

    @Test
    void getMessageTest() {
        Assertions.assertEquals(message, notification.getMessage());
    }

    @Test
    void setMessageTest() {
        Notification notification = new Notification();
        notification.setMessage(newMessage);
        Assertions.assertEquals(newMessage, notification.getMessage());
    }

    @Test
    void equalsSameTest() {
        Assertions.assertEquals(notification, notification);
    }

    @Test
    void equalsAnotherObjectTest() {
        Assertions.assertNotEquals(notification, 42);
    }

    @Test
    void equalsEqualTest() {
        Notification sameNotification = new Notification(notificationId, userId, message);
        Assertions.assertEquals(notification, sameNotification);
    }

    @Test
    void equalsDifferentMessageTest() {
        Notification notSameNotification = new Notification(notificationId, userId, newMessage);
        Assertions.assertNotEquals(notification, notSameNotification);
    }

    @Test
    void equalsDifferentNotificationIdTest() {
        Notification notSameNotification = new Notification(13L, userId, newMessage);
        Assertions.assertNotEquals(notification, notSameNotification);
    }

    @Test
    void equalsDifferentUserIdTest() {
        Notification notSameNotification = new Notification(notificationId, 1L, newMessage);
        Assertions.assertNotEquals(notification, notSameNotification);
    }

    @Test
    void hashCodeSameTest() {
        Notification otherNotification = new Notification(notificationId, userId, message);
        Assertions.assertEquals(notification.hashCode(), otherNotification.hashCode());
    }

    @Test
    void hashCodeDifferentTest() {
        Notification otherNotification = new Notification(notificationId, userId, newMessage);
        Assertions.assertNotEquals(notification.hashCode(), otherNotification.hashCode());
    }
}