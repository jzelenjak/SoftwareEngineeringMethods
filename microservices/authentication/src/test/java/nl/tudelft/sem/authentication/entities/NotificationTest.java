package nl.tudelft.sem.authentication.entities;

import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NotificationTest {
    private transient Notification notification;

    // Some fixed values we use often.
    private final transient long notificationId = 42L;
    private final transient long userId = 1234567L;
    private final transient String message = "Hey there, you are hired!";
    private final transient String newMessage = "Unfortunately you are fired, get out.";
    private final transient LocalDateTime notificationDate = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        notification = new Notification(userId, message);
    }

    @Test
    void getNotificationIdTest() {
        notification.setNotificationId(notificationId);
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
    void getNotificationDateTest() {
        Assertions.assertEquals(notificationDate.toLocalDate(),
                notification.getNotificationDate().toLocalDate());
    }

    @Test
    void setNotificationDateTest() {
        Notification notification = new Notification();
        notification.setNotificationDate(notificationDate);
        Assertions.assertEquals(notificationDate, notification.getNotificationDate());
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
        Notification sameNotification = new Notification(userId, message);
        Assertions.assertEquals(notification, sameNotification);
    }

    @Test
    void equalsDifferentMessageTest() {
        Notification notSameNotification = new Notification(userId, newMessage);
        Assertions.assertNotEquals(notification, notSameNotification);
    }

    @Test
    void equalsDifferentNotificationIdTest() {
        Notification notSameNotification = new Notification(userId, newMessage);
        notification.setNotificationId(123321L);
        notSameNotification.setNotificationId(321123L);
        Assertions.assertNotEquals(notification, notSameNotification);
    }

    @Test
    void equalsDifferentUserIdTest() {
        Notification notSameNotification = new Notification(1L, newMessage);
        Assertions.assertNotEquals(notification, notSameNotification);
    }

    @Test
    void hashCodeSameTest() {
        Notification otherNotification = new Notification(userId, message);
        Assertions.assertEquals(notification.hashCode(), otherNotification.hashCode());
    }

    @Test
    void hashCodeDifferentTest() {
        Notification otherNotification = new Notification(userId, newMessage);
        Assertions.assertNotEquals(notification.hashCode(), otherNotification.hashCode());
    }

    @Test
    void toJsonSuccessTest() {
        Notification someNotification = new Notification(userId, message);
        LocalDateTime timeStamp = someNotification.getNotificationDate();
        String json = String.format("{\"message\":\"%s\",\"notificationDate\":\"%s\"}",
                this.message, timeStamp.getHour()
                        + ":" + timeStamp.getMinute()
                        + " " + timeStamp.getDayOfMonth()
                        + "-" + timeStamp.getMonthValue()
                        + "-" + timeStamp.getYear()
                        + " " + ZoneId.systemDefault());
        Assertions.assertEquals(json, someNotification.toJsonResponse());
    }

    @Test
    void toJsonFailedTest() {
        Notification someNotification = new Notification(userId, message);
        String json = String.format("{\"message\":\"%s,\"notificationDate\":\"%s\"}",
                this.message, this.notificationDate.toLocalDate().toString());
        Assertions.assertNotEquals(json, someNotification.toJsonResponse());
    }
}