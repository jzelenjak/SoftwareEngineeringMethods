package nl.tudelft.sem.authentication.repositories;

import java.util.List;
import java.util.Optional;
import nl.tudelft.sem.authentication.entities.Notification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


/**
 * The repository for storing notification data.
 */
@Repository
public interface NotificationRepository extends CrudRepository<Notification, Long> {

    /**
     * Finds Notification by their id.
     *
     * @param notificationId the id of the notification.
     * @return the notification if found.
     */
    Optional<Notification> findByNotificationId(long notificationId);


    Optional<List<Notification>> findByUserId(long userId);
}
