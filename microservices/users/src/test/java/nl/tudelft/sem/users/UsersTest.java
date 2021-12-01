package nl.tudelft.sem.users;

import nl.tudelft.sem.users.entities.User;
import nl.tudelft.sem.users.entities.UserRole;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UsersTest {
    private transient User user1;


    @BeforeEach
    public void setup() {
        user1 = new User("S.Tudent@student.tudelft.nl", "ThisIsMyPassword42",
                "Steve", "Tudent", UserRole.STUDENT);
    }

    @Test
    public void constructorTest() {
        Assertions.assertEquals(user1.getUsername(), "S.Tudent@student.tudelft.nl");
        Assertions.assertEquals(user1.getPassword(), "ThisIsMyPassword42");
        Assertions.assertEquals(user1.getFirstName(), "Steve");
        Assertions.assertEquals(user1.getLastName(), "Tudent");
        Assertions.assertEquals(user1.getRole(), UserRole.STUDENT);
    }

    @Test
    public void setterTests() {
        String username = "A.Sadman@tudelft.nl";
        String password = "NoFraudAllowed";
        String firstName = "Andy";
        String lastName = "Sadman";
        UserRole role = UserRole.ADMIN;
        user1.setUsername(username);
        user1.setPassword(password);
        user1.setFirstName(firstName);
        user1.setLastName(lastName);
        user1.setRole(role);
        Assertions.assertEquals(user1.getUsername(), "A.Sadman@tudelft.nl");
        Assertions.assertEquals(user1.getPassword(), "NoFraudAllowed");
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
        Assertions.assertNull(ghost.getPassword());
    }

    @Test
    public void roleTest() {
        user1.setRole(UserRole.TA);
        Assertions.assertEquals(user1.getRole(), UserRole.TA);
        user1.setRole(UserRole.CANDIDATE_TA);
        Assertions.assertEquals(user1.getRole(), UserRole.CANDIDATE_TA);
    }
}
