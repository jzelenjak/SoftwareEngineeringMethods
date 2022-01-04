package nl.tudelft.sem.authentication.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.ZonedDateTime;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;


/**
 * A class for storing notifications.
 */
@Entity(name = "notifications")
public class Notification {
    @Id
    @Column(name = "notificationId", unique = true, nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long notificationId;

    @Column(name = "userId", nullable = false)
    private long userId;

    @Column(name = "message", nullable = false)
    private String message;

    @Column(name = "notificationDate", columnDefinition = "TIMESTAMP")
    private ZonedDateTime notificationDate;


    /**
     * An empty constructor to create a notification.
     */
    public Notification() {
    }

    /**
     * Instantiates a new Notification.
     *
     * @param userId         the user id connected to the notification.
     * @param message        the message of the notification.
     */
    public Notification(long userId, String message) {
        this.userId = userId;
        this.message = message;
        this.notificationDate = ZonedDateTime.now();
    }

    /**
     * Gets notification id.
     *
     * @return notification id.
     */
    public long getNotificationId() {
        return this.notificationId;
    }

    /**
     * Sets notification id.
     *
     * @param notificationId the notification id.
     */
    public void setNotificationId(long notificationId) {
        this.notificationId = notificationId;
    }

    /**
     * Gets the user ID of the user connected to the notification.
     *
     * @return the user ID of the user.
     */
    public long getUserId() {
        return this.userId;
    }

    /**
     * Sets the user ID of the user connected to the notification.
     *
     * @param userId the user ID of the user.
     */
    public void setUserId(long userId) {
        this.userId = userId;
    }

    /**
     * Gets message/content of the notification.
     *
     * @return message of the notification.
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Sets message of the notification.
     *
     * @param message the message of the notification.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Gets notification date.
     *
     * @return notification date.
     */
    public ZonedDateTime getNotificationDate() {
        return this.notificationDate;
    }

    /**
     * Sets notification date.
     *
     * @param notificationDate the notification date
     */
    public void setNotificationDate(ZonedDateTime notificationDate) {
        this.notificationDate = notificationDate;
    }

    /**
     * Checks if another notification is equal to this notification.
     *
     * @param other the object ot compare to
     * @return true if the other object is the same as this object, otherwise false.
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Notification)) {
            return false;
        }

        Notification that = (Notification) other;
        return this.notificationId == that.notificationId
                && this.userId == that.userId
                && this.message.equals(that.message);
    }

    /**
     * Returns the hash code of a Notification object.
     *
     * @return the hash code of a Notification object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(notificationId, userId, message);
    }

    /**
     * Turn the entity into a JSON response message.
     *
     * <i>Example:</i>
     * <pre>
     *  {
     *      "message" : "Hey there, you are hired!",
     *      "notificationDate" : "17:54 10-12-2021 Europe/Berlin"
     *  }
     * </pre>
     *
     * @return a string representation of the Notification.
     */
    public String toJsonResponse() {
        String date = this.notificationDate.getHour()
                + ":" + this.notificationDate.getMinute()
                + " " + this.notificationDate.getDayOfMonth()
                + "-" + this.notificationDate.getMonthValue()
                + "-" + this.notificationDate.getYear()
                + " " + this.notificationDate.getZone();

        return new ObjectMapper().createObjectNode()
                .put("message", this.message)
                .put("notificationDate", date)
                .toPrettyString();
    }
}
