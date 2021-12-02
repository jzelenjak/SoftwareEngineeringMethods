package nl.tudelft.sem.entities;

import nl.tudelft.sem.entities.entities.User;
import nl.tudelft.sem.entities.entities.UserRole;
import nl.tudelft.sem.entities.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class UserTest {
    @Autowired
    private transient UserRepository repo;
    private transient User user1;
    private final transient String netId = "S.Tudent@student.tudelft.nl";
    private final transient String lastName = "Tudent";

    @BeforeEach
    public void setup() {
        repo.save(new User(netId, "Steve", lastName, UserRole.STUDENT));
        user1 = repo.findByUsername(netId).get();
    }

    @Test
    public void constructorTest() {
        Assertions.assertNotEquals(0, user1.getUserId());
        Assertions.assertEquals(user1.getUsername(), netId);
        Assertions.assertEquals(user1.getFirstName(), "Steve");
        Assertions.assertEquals(user1.getLastName(), lastName);
        Assertions.assertEquals(user1.getRole(), UserRole.STUDENT);

        repo.delete(user1);
    }

    @Test
    public void setterTests() {
        User user = new User();

        user.setUsername("A.Sadman@tudelft.nl");
        user.setFirstName("Andy");
        user.setLastName("Sadman");
        user.setRole(UserRole.ADMIN);

        repo.save(user);
        user = repo.findByUsername("A.Sadman@tudelft.nl").get();

        Assertions.assertEquals(user.getUsername(), "A.Sadman@tudelft.nl");
        Assertions.assertEquals(user.getFirstName(), "Andy");
        Assertions.assertEquals(user.getLastName(), "Sadman");
        Assertions.assertEquals(user.getRole(), UserRole.ADMIN);

        repo.delete(user);
        repo.delete(user1);
    }

    @Test
    public void roleTest() {
        user1.setRole(UserRole.TA);
        Assertions.assertEquals(user1.getRole(), UserRole.TA);

        user1.setRole(UserRole.CANDIDATE_TA);
        Assertions.assertEquals(user1.getRole(), UserRole.CANDIDATE_TA);

        repo.delete(user1);
    }

    @Test
    public void equalsSameTest() {
        Assertions.assertEquals(user1, user1);

        repo.delete(user1);
    }

    @Test
    public void equalsDifferentClassTest() {
        Assertions.assertNotEquals(user1, "Forty-two");

        repo.delete(user1);
    }

    @Test
    public void equalsDifferentSameUsernameTest() {
        repo.delete(user1); // to avoid conflicts

        User user2 = new User(netId, "Steve", lastName, UserRole.LECTURER);
        repo.save(user2);
        user2 = repo.findByUsername(netId).get();
        repo.delete(user2);

        Assertions.assertNotEquals(user1, user2);
    }

    @Test
    public void equalsDifferentDifferentUsernamesTest() {
        repo.delete(user1); // to avoid conflicts

        User user2 = new User("S.Tud@student.tudelft.nl",
                "Steven", "Tud", UserRole.STUDENT);
        repo.save(user2);
        user2 = repo.findByUsername("S.Tud@student.tudelft.nl").get();
        repo.delete(user2);

        Assertions.assertNotEquals(user1, user2);
    }

    @Test
    public void hashCodeEqualTest() {
        repo.delete(user1); // to avoid conflicts

        User user2 = new User(netId,
                "Stefan", lastName, UserRole.ADMIN);
        repo.save(user2);
        user2 = repo.findByUsername(netId).get();
        repo.delete(user2);

        Assertions.assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    public void hashCodeDifferentTest() {
        repo.delete(user1); // to avoid conflicts

        User user2 = new User("S.Tudentin@tudelft.nl", "Stefan",
                "Tudentin", UserRole.ADMIN);
        repo.save(user2);
        user2 = repo.findByUsername("S.Tudentin@tudelft.nl").get();
        repo.delete(user2);

        Assertions.assertNotEquals(user1.hashCode(), user2.hashCode());
    }
}
