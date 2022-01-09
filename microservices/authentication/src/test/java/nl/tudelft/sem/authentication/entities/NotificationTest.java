package nl.tudelft.sem.authentication.entities;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
    private final transient ZonedDateTime notificationDate = ZonedDateTime.now();

    @BeforeEach
    void setUp() {
        notification = new Notification(userId, message);
    }

    @Test
    void testGetNotificationId() {
        notification.setNotificationId(notificationId);
        Assertions.assertEquals(notificationId, notification.getNotificationId());
    }

    @Test
    void testSetNotificationId() {
        Notification notification = new Notification();
        notification.setNotificationId(12L);
        Assertions.assertEquals(12L, notification.getNotificationId());
    }

    @Test
    void testGetUserId() {
        Assertions.assertEquals(userId, notification.getUserId());
    }

    @Test
    void testSetUserId() {
        Notification notification = new Notification();
        notification.setUserId(userId);
        Assertions.assertEquals(userId, notification.getUserId());
    }

    @Test
    void testGetMessage() {
        Assertions.assertEquals(message, notification.getMessage());
    }

    @Test
    void testSetMessage() {
        Notification notification = new Notification();
        notification.setMessage(newMessage);
        Assertions.assertEquals(newMessage, notification.getMessage());
    }

    @Test
    void testGetNotificationDate() {
        Assertions.assertEquals(notificationDate.toLocalDate(),
                notification.getNotificationDate().toLocalDate());
    }

    @Test
    void testSetNotificationDate() {
        Notification notification = new Notification();
        notification.setNotificationDate(notificationDate);
        Assertions.assertEquals(notificationDate, notification.getNotificationDate());
    }

    @Test
    void testEqualsSame() {
        Assertions.assertEquals(notification, notification);
    }

    @Test
    void testEqualsAnotherObject() {
        Assertions.assertNotEquals(notification, 42);
    }

    @Test
    void testEqualsEqual() {
        Notification sameNotification = new Notification(userId, message);
        Assertions.assertEquals(notification, sameNotification);
    }

    @Test
    void testEqualsDifferentMessage() {
        Notification notSameNotification = new Notification(userId, newMessage);
        Assertions.assertNotEquals(notification, notSameNotification);
    }

    @Test
    void testEqualsDifferentNotificationId() {
        Notification notSameNotification = new Notification(userId, newMessage);
        notification.setNotificationId(123321L);
        notSameNotification.setNotificationId(321123L);
        Assertions.assertNotEquals(notification, notSameNotification);
    }

    @Test
    void testEqualsDifferentUserId() {
        Notification notSameNotification = new Notification(1L, newMessage);
        Assertions.assertNotEquals(notification, notSameNotification);
    }

    @Test
    void testHashCodeSame() {
        Notification otherNotification = new Notification(userId, message);
        Assertions.assertEquals(notification.hashCode(), otherNotification.hashCode());
    }

    @Test
    void testHashCodeDifferent() {
        Notification otherNotification = new Notification(userId, newMessage);
        Assertions.assertNotEquals(notification.hashCode(), otherNotification.hashCode());
    }

    @Test
    void testToJsonSuccess() {
        Notification someNotification = new Notification(userId, message);
        ZonedDateTime timeStamp = someNotification.getNotificationDate();
        String date = timeStamp.getHour()
                + ":" + timeStamp.getMinute()
                + " " + timeStamp.getDayOfMonth()
                + "-" + timeStamp.getMonthValue()
                + "-" + timeStamp.getYear()
                + " " + ZoneId.systemDefault();

        String actualJsonResponse = someNotification.toJsonResponse();
        String expectedJsonResponse = createPrettyJson("message",
                this.message, "notificationDate", date);
        Assertions.assertEquals(expectedJsonResponse, actualJsonResponse);
    }

    @Test
    void testToJsonFailed() {
        Notification someNotification = new Notification(userId, message);
        String json = String.format("{\"message\":\"%s,\"notificationDate\":\"%s\"}",
                this.message, this.notificationDate.toLocalDate().toString());
        Assertions.assertNotEquals(json, someNotification.toJsonResponse());
    }

    /**
     * A helper method to create json pretty string out of String key-value pairs.
     *
     * @param kvPairs       list of key-values, must be an even number
     * @return the json string that can be used in the response body
     */
    private String createPrettyJson(String... kvPairs) {
        ObjectNode node = new ObjectMapper().createObjectNode();

        for (int i = 0; i < kvPairs.length; i += 2) {
            node.put(kvPairs[i], kvPairs[i + 1]);
        }
        return node.toPrettyString();
    }
}