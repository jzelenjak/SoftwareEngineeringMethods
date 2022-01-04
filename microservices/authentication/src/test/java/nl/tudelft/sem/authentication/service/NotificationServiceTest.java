package nl.tudelft.sem.authentication.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import nl.tudelft.sem.authentication.entities.Notification;
import nl.tudelft.sem.authentication.repositories.NotificationRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.EntityNotFoundException;


@SpringBootTest
class NotificationServiceTest {
    @Autowired
    private transient NotificationService notificationService;

    @Autowired
    private transient NotificationRepository notificationRepository;

    @Test
    void testAddNotificationSuccess() {
        Assertions.assertTrue(this.notificationService
                .addNewNotification(1324356L, "You have been promoted to head TA!"));
    }

    @Test
    void testAddNotificationWithAnotherNotificationSuccess() {
        Notification notification = new Notification(1234567L, "You have been selected!");
        this.notificationRepository.save(notification);
        Assertions.assertTrue(this.notificationService
                .addNewNotification(7654321L, "You have been promoted to head TA!"));
        this.notificationRepository.delete(notification);
    }

    @Test
    void testChangeUserFromNotificationSuccess() {
        final long userId = 5534985230058255333L;
        this.notificationRepository.save(new Notification(userId, "You have been selected!"));

        Notification first = notificationService
                .loadNotificationByUserId(userId)
                .get(0);

        final long notificationId = first.getNotificationId();
        final long newUserId = 5555334L;
        this.notificationService.changeUserIdFromNotification(notificationId, newUserId);

        Optional<Notification> optionalNotification = notificationRepository
                .findByNotificationId(notificationId);

        assert optionalNotification.isPresent();
        Notification notification = optionalNotification.get();

        Assertions.assertEquals(newUserId, notification.getUserId());

        this.notificationRepository.delete(notification);
    }

    @Test
    void testChangeUserFromNotificationNotFound() {
        Assertions.assertThrows(EntityNotFoundException.class, () -> this.notificationService
                        .changeUserIdFromNotification(11L, 5555334L),
                String.format("Notification with id %d has not been found", 11L));
    }

    @Test
    void testChangeMessageFromNotificationSuccess() {
        final long userId = 55415553368669L;
        this.notificationRepository.save(new Notification(userId, "You have been selected!"));

        Notification first = notificationService
                .loadNotificationByUserId(userId)
                .get(0);

        final long notificationId = first.getNotificationId();
        final String newMessage = "You have been rejected.";
        this.notificationService.changeMessageFromNotification(notificationId,
                newMessage);

        Optional<Notification> optionalNotification = notificationRepository
                .findByNotificationId(notificationId);

        assert optionalNotification.isPresent();
        Notification notification = optionalNotification.get();

        Assertions.assertEquals(newMessage, notification.getMessage());

        this.notificationRepository.delete(notification);
    }

    @Test
    void testChangeMessageFromNotificationNotFound() {
        Assertions.assertThrows(EntityNotFoundException.class, () -> this.notificationService
                    .changeMessageFromNotification(13L,
                            "Your hours have been approved."),
                String.format("Notification with id %d has not been found", 13L));
    }

    @Test
    void testLoadByNotificationIdFound() {
        final long userId = 4477939004820391L;
        Notification notification = new Notification(userId, "You have not been selected.");
        this.notificationRepository.save(notification);

        Notification first = notificationService
                .loadNotificationByUserId(userId)
                .get(0);

        final long notificationId = first.getNotificationId();

        Assertions.assertEquals(notification, this.notificationService
                        .loadNotificationByNotificationId(notificationId));

        this.notificationRepository.delete(notification);
    }

    @Test
    void testLoadByNotificationIdNotFound() {
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> this.notificationService.loadNotificationByNotificationId(12345678L));
    }

    @Test
    void testLoadByUserIdFound() {
        final long userId = 2913889L;
        Notification notification1 = new Notification(userId, "Your hours have been rejected.");
        this.notificationRepository.save(notification1);

        Notification notification2 = new Notification(userId, "Your contract has been voided.");
        this.notificationRepository.save(notification2);

        List<Notification> notificationList = new ArrayList<>();
        notificationList.add(notification1);
        notificationList.add(notification2);

        List<Notification> actualList = this.notificationService.loadNotificationByUserId(userId);
        Assertions.assertEquals(notificationList.size(), actualList.size());
        for (int i = 0; i < notificationList.size() - 1; i++) {
            Assertions.assertEquals(notificationList.get(i), actualList.get(i));
        }

        this.notificationRepository.deleteAll(notificationList);
    }

    @Test
    void testLoadByUserIdNotFound() {
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> this.notificationService.loadNotificationByUserId(12345678L));
    }

    @Test
    void testDeleteNotificationByNotificationIdSuccess() {
        final long userId = 9665145305021934L;
        this.notificationRepository.save(
                new Notification(userId, "Your contract has been extended!"));

        Notification first = notificationService
                .loadNotificationByUserId(userId)
                .get(0);

        final long notificationId = first.getNotificationId();

        Optional<Notification> beforeDeletionNotification = notificationRepository
                .findByNotificationId(notificationId);

        assert beforeDeletionNotification.isPresent();

        this.notificationService.deleteNotificationByNotificationId(notificationId);

        Optional<Notification> afterDeletionNotification = notificationRepository
                .findByNotificationId(notificationId);

        assert afterDeletionNotification.isEmpty();
    }

    @Test
    void testDeleteNotificationByNotificationIdFailed() {
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> this.notificationService.deleteNotificationByNotificationId(12345678L));
    }

    @Test
    void testDeleteNotificationsByUserIdSuccess() {
        final long userId = 9651548L;
        this.notificationRepository.save(
                new Notification(userId, "Your application has been withdrawn."));

        Optional<List<Notification>> beforeDeletionNotification = notificationRepository
                .findByUserId(userId);

        assert beforeDeletionNotification.isPresent();
        Assertions.assertNotEquals(new ArrayList<Notification>(), beforeDeletionNotification.get());

        this.notificationService.deleteNotificationsFromUser(userId);

        Optional<List<Notification>> afterDeletionNotification = notificationRepository
                .findByUserId(userId);

        assert afterDeletionNotification.isEmpty();
    }

    @Test
    void testDeleteNotificationsByUserIdFailed() {
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> this.notificationService.deleteNotificationsFromUser(12345678L));
    }
}