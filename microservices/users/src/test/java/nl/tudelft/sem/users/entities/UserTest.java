package nl.tudelft.sem.users.entities;

import nl.tudelft.sem.users.repositories.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;


@SpringBootTest
class UserTest {

    @Autowired
    private transient UserRepository repo;

    private transient User user1;

    private final transient String username = "S.Tudent@student.tudelft.nl";


    @BeforeEach
    void setUp() {
        user1 = repo.save(new User(username, "Steve", "Tudent", UserRole.STUDENT));
    }

    @AfterEach
    void clean() {
        this.repo.deleteAll();
    }

    @Test
    void testConstructor() {
        Assertions
            .assertThat(new User("vladvlad@student.tudelft.nl",
                    "Vlad", "Vlad", UserRole.STUDENT))
            .isNotNull();
    }

    @Test
    void testEmptyConstructor() {
        Assertions
            .assertThat(new User())
            .isNotNull();
    }


    @Test
    void testGetUserId() {
        Assertions
            .assertThat(user1.getUserId())
            .isGreaterThan(0);
    }

    @Test
    void testSetUserIdCanUpdateSame() {
        long oldId = user1.getUserId();
        user1.setUserId(user1.getUserId());
        user1 = this.repo.save(user1);

        Assertions
            .assertThat(user1.getUserId())
            .isEqualTo(oldId);
    }


    @Test
    void testSetUserIdCannotUpdateDifferent() {
        user1.setUserId(42L);

        Assertions
            .assertThatThrownBy(() -> this.repo.save(user1))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void testGetUsername() {
        Assertions
            .assertThat(user1.getUsername())
            .isEqualTo(username);
    }


    @Test
    void testSetUsernameIsUpdatable() {
        user1.setUsername("hahahahahaha@student.tudelft.nl");
        user1 = this.repo.save(user1);

        Assertions
            .assertThat(user1.getUsername())
            .isEqualTo("hahahahahaha@student.tudelft.nl");
    }


    @Test
    void testSetUsernameMustBeUniq() {
        Assertions
            .assertThatThrownBy(() ->
                    this.repo.save(new User(username, "amogus", "sus", UserRole.STUDENT)))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void testGetFirstName() {
        Assertions
            .assertThat(user1.getFirstName())
            .isEqualTo("Steve");
    }

    @Test
    void testSetFirstName() {
        user1.setFirstName("Val");

        Assertions
            .assertThat(user1.getFirstName())
            .isEqualTo("Val");
    }

    @Test
    void testGetLastName() {
        Assertions
            .assertThat(user1.getLastName())
            .isEqualTo("Tudent");
    }

    @Test
    void testSetLastName() {
        user1.setLastName("Python");

        Assertions
            .assertThat(user1.getLastName())
            .isEqualTo("Python");
    }

    @Test
    void testGetRole() {
        Assertions
            .assertThat(user1.getRole())
            .isEqualTo(UserRole.STUDENT);
    }

    @Test
    void testSetRole() {
        user1.setRole(UserRole.LECTURER);

        Assertions
            .assertThat(user1.getRole())
            .isEqualTo(UserRole.LECTURER);
    }

    @Test
    void testEqualsSame() {
        Assertions
            .assertThat(user1)
            .isEqualTo(user1);
    }

    @Test
    void testEqualsEqual() {
        User user2 = new User("ddelft@tudelft.nl", "delta", "delft", UserRole.LECTURER);
        user2.setUserId(user1.getUserId());

        Assertions
            .assertThat(user1)
            .isEqualTo(user2);
    }

    @Test
    void testEqualsDifferentClass() {
        Assertions
            .assertThat(user1)
            .isNotEqualTo(Long.valueOf(user1.getUserId()).toString());
    }

    @Test
    void testEqualsDifferent() {
        User user2 = new User("ddelft@tudelft.nl", "delta", "delft", UserRole.LECTURER);
        user2 = this.repo.save(user2);

        Assertions
            .assertThat(user1)
            .isNotEqualTo(user2);

        this.repo.delete(user2);
    }

    @Test
    void testEqualsDifferentButSameNetId() {
        User user2 = new User("ddelft@tudelft.nl", "delta", "delft", UserRole.LECTURER);
        user2 = this.repo.save(user2);
        user2.setUsername(username);

        Assertions
            .assertThat(user1)
            .isNotEqualTo(user2);

        this.repo.deleteById(user2.getUserId());
    }

    @Test
    void testHashCodeSame() {
        User user2 = new User("ddos@tudelft.nl", "d", "dos", UserRole.STUDENT);
        user2.setUserId(user1.getUserId());

        Assertions
            .assertThat(user2.hashCode())
            .isEqualTo(user1.hashCode());
    }

    @Test
    void testHashCodeDifferent() {
        User user2 = new User("S" + username, user1.getFirstName(),
                user1.getLastName(), UserRole.STUDENT);
        this.repo.save(user2);

        Assertions
            .assertThat(user2.hashCode())
            .isNotEqualTo(user1.hashCode());

        this.repo.delete(user2);
    }
}