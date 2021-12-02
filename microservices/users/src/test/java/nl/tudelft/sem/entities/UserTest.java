package nl.tudelft.sem.entities;

import nl.tudelft.sem.entities.entities.User;
import nl.tudelft.sem.entities.entities.UserRole;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UserTest {
    private transient User user1;
    private final transient String netId = "S.Tudent@student.tudelft.nl";
    private final transient String lastName = "Tudent";

    @BeforeEach
    public void setup() {
        user1 = new User(netId, "Steve", lastName, UserRole.STUDENT);
    }

    @Test
    public void constructorTest() {
        Assertions.assertEquals(user1.getUsername(), netId);
        Assertions.assertEquals(user1.getFirstName(), "Steve");
        Assertions.assertEquals(user1.getLastName(), lastName);
        Assertions.assertEquals(user1.getRole(), UserRole.STUDENT);
    }

    @Test
    public void setterTests() {
        String username = "A.Sadman@tudelft.nl";
        String firstName = "Andy";
        String lastName = "Sadman";
        UserRole role = UserRole.ADMIN;
        user1.setUsername(username);
        user1.setFirstName(firstName);
        user1.setLastName(lastName);
        user1.setRole(role);
        Assertions.assertEquals(user1.getUsername(), "A.Sadman@tudelft.nl");
        Assertions.assertEquals(user1.getFirstName(), "Andy");
        Assertions.assertEquals(user1.getLastName(), "Sadman");
        Assertions.assertEquals(user1.getRole(), UserRole.ADMIN);
    }

    @Test
    public void emptyConstructorTest() {
        User ghost = new User();
        Assertions.assertNull(ghost.getRole());
        Assertions.assertNull(ghost.getUsername());
        Assertions.assertNull(ghost.getFirstName());
        Assertions.assertNull(ghost.getLastName());
    }

    @Test
    public void roleTest() {
        user1.setRole(UserRole.TA);
        Assertions.assertEquals(user1.getRole(), UserRole.TA);
        user1.setRole(UserRole.CANDIDATE_TA);
        Assertions.assertEquals(user1.getRole(), UserRole.CANDIDATE_TA);
    }

    @Test
    public void equalsSameTest() {
        Assertions.assertEquals(user1, user1);
    }

    @Test
    public void equalsDifferentClassTest() {
        Assertions.assertNotEquals(user1, "Forty-two");
    }

    @Test
    public void equalsEqualTest() {
        User user2 = new User(netId,
                "Steve", lastName, UserRole.LECTURER);
        Assertions.assertEquals(user1, user2);
    }

    @Test
    public void equalsDifferentTest() {
        User user2 = new User("S.Tud@student.tudelft.nl",
                "Steven", "Tud", UserRole.STUDENT);
        Assertions.assertNotEquals(user1, user2);
    }

    @Test
    public void hashCodeEqualTest() {
        User user2 = new User(netId,
                "Stefan", lastName, UserRole.ADMIN);
        Assertions.assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    public void hashCodeDifferentTest() {
        User user2 = new User("S.Tudentin@tudelft.nl", "Stefan",
                "Tudentin", UserRole.ADMIN);
        Assertions.assertNotEquals(user1.hashCode(), user2.hashCode());
    }
}
