package nl.tudelft.sem.authentication.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityNotFoundException;
import nl.tudelft.sem.authentication.entities.Notification;
import nl.tudelft.sem.authentication.entities.UserData;
import nl.tudelft.sem.authentication.repositories.NotificationDataRepository;
import nl.tudelft.sem.authentication.repositories.UserDataRepository;
import nl.tudelft.sem.authentication.security.UserRole;
import org.aspectj.weaver.ast.Not;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;


@SpringBootTest
class NotificationServiceTest {
    @Autowired
    private transient NotificationService notificationService;

    @Autowired
    private transient NotificationDataRepository notificationDataRepository;

    @Test
    void addNotificationSuccessTest() {
        Assertions.assertTrue(this.notificationService
                .addNewNotification(2L, 1324356L,
                        "You have been promoted to head TA!"));
    }

    @Test
    void addExistingNotificationFailedTest() {
        final long notificationId = 1L;
        Notification notification = new Notification(
                notificationId, 1234567L, "You have been selected!");
        this.notificationDataRepository.save(notification);
        Assertions.assertFalse(this.notificationService.addNewNotification(
                notificationId, 7654321L, "You have been promoted to head TA!"));
        this.notificationDataRepository.delete(notification);
    }

    @Test
    void changeUserFromNotificationSuccessTest() {
        final long notificationId = 10L;
        final long userId = 5555333L;
        this.notificationDataRepository.save(
                new Notification(notificationId, userId, "You have been selected!"));

        final long newUserId = 5555334L;
        this.notificationService.changeUserIdFromNotification(notificationId, newUserId);

        Optional<Notification> optionalNotification = notificationDataRepository
                .findByNotificationId(notificationId);

        assert optionalNotification.isPresent();
        Notification notification = optionalNotification.get();

        Assertions.assertEquals(newUserId, notification.getUserId());

        this.notificationDataRepository.delete(notification);
    }

    @Test
    void changeUserFromNotificationNotFoundTest() {
        Assertions.assertThrows(EntityNotFoundException.class, () -> this.notificationService
                        .changeUserIdFromNotification(11L, 5555334L),
                String.format("Notification with id %d has not been found", 11L));
    }

    @Test
    void changeMessageFromNotificationSuccessTest() {
        final long notificationId = 21L;
        final long userId = 5555336L;
        this.notificationDataRepository.save(
                new Notification(notificationId, userId, "You have been selected!"));

        final String newMessage = "You have been rejected.";
        this.notificationService.changeMessageFromNotification(notificationId,
                newMessage);

        Optional<Notification> optionalNotification = notificationDataRepository
                .findByNotificationId(notificationId);

        assert optionalNotification.isPresent();
        Notification notification = optionalNotification.get();

        Assertions.assertEquals(newMessage, notification.getMessage());

        this.notificationDataRepository.delete(notification);
    }

    @Test
    void changeMessageFromNotificationNotFoundTest() {
        Assertions.assertThrows(EntityNotFoundException.class, () -> this.notificationService
                    .changeMessageFromNotification(13L,
                            "Your hours have been approved."),
                String.format("Notification with id %d has not been found", 13L));
    }

    @Test
    void loadByNotificationIdFoundTest() {
        final long notificationId = 55L;
        final long userId = 4477991L;
        Notification notification = new Notification(
                notificationId, userId, "You have not been selected.");
        this.notificationDataRepository.save(notification);

        Assertions.assertEquals(notification, this.notificationService
                        .loadNotificationByNotificationId(notificationId));

        this.notificationDataRepository.delete(notification);
    }

    @Test
    void loadByNotificationIdNotFoundTest() {
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> this.notificationService.loadNotificationByNotificationId(12345678L));
    }

    @Test
    void loadByUserIdFoundTest() {
        final long notificationId = 520L;
        final long userId = 2913889L;
        Notification notification1 = new Notification(
                notificationId, userId, "Your hours have been rejected.");
        this.notificationDataRepository.save(notification1);

        Notification notification2 = new Notification(notificationId + 1, userId,
                "Your contract has been voided.");
        this.notificationDataRepository.save(notification2);

        List<Notification> notificationList = new ArrayList<>();
        notificationList.add(notification1);
        notificationList.add(notification2);

        List<Notification> actualList = this.notificationService.loadNotificationByUserId(userId);
        Assertions.assertEquals(notificationList.size(), actualList.size());
        for (int i = 0; i < notificationList.size() - 1; i++) {
            Assertions.assertEquals(notificationList.get(i), actualList.get(i));
        }

        this.notificationDataRepository.deleteAll(notificationList);
    }

    @Test
    void loadByUserIdNotFoundTest() {
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> this.notificationService.loadNotificationByUserId(12345678L));
    }

    @Test
    void deleteNotificationByNotificationIdSuccessTest() {
        final long notificationId = 950L;
        final long userId = 6651934L;
        this.notificationDataRepository.save(
                new Notification(notificationId, userId, "Your contract has been extended!"));

        Optional<Notification> beforeDeletionNotification = notificationDataRepository
                .findByNotificationId(notificationId);

        assert beforeDeletionNotification.isPresent();

        this.notificationService.deleteNotificationByNotificationId(notificationId);

        Optional<Notification> afterDeletionNotification = notificationDataRepository
                .findByNotificationId(notificationId);

        assert afterDeletionNotification.isEmpty();
    }

    @Test
    void deleteNotificationByNotificationIdFailedTest() {
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> this.notificationService.deleteNotificationByNotificationId(12345678L));
    }

    @Test
    void deleteNotificationsByUserIdSuccessTest() {
        final long notificationId = 999L;
        final long userId = 9651548L;
        this.notificationDataRepository.save(
                new Notification(notificationId, userId, "Your application has been withdrawn."));

        Optional<List<Notification>> beforeDeletionNotification = notificationDataRepository
                .findByUserId(userId);

        assert beforeDeletionNotification.isPresent();
        Assertions.assertNotEquals(new ArrayList<Notification>(), beforeDeletionNotification.get());

        this.notificationService.deleteNotificationsFromUser(userId);

        Optional<List<Notification>> afterDeletionNotification = notificationDataRepository
                .findByUserId(userId);

        assert afterDeletionNotification.isEmpty();
    }

    @Test
    void deleteNotificationsByUserIdFailedTest() {
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> this.notificationService.deleteNotificationsFromUser(12345678L));
    }
}