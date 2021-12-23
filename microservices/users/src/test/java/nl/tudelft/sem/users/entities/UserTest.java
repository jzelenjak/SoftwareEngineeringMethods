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

    private final transient String netId = "S.Tudent@student.tudelft.nl";


    @BeforeEach
    void setUp() {
        user1 = repo.save(new User(netId, "Steve", "Tudent", UserRole.STUDENT));
    }

    @AfterEach
    void clean() {
        this.repo.deleteAll();
    }

    @Test
    void constructorTest() {
        Assertions
                .assertThat(new User("vladvlad@student.tudelft.nl",
                        "Vlad", "Vlad", UserRole.STUDENT))
                .isNotNull();
    }

    @Test
    void emptyConstructorTest() {
        Assertions
                .assertThat(new User())
                .isNotNull();
    }


    @Test
    void getUserIdTest() {
        Assertions
                .assertThat(user1.getUserId())
                .isGreaterThan(0);
    }

    @Test
    void setUserIdCanUpdateSameTest() {
        long oldId = user1.getUserId();
        user1.setUserId(user1.getUserId());
        user1 = this.repo.save(user1);

        Assertions
                .assertThat(user1.getUserId())
                .isEqualTo(oldId);
    }


    @Test
    void setUserIdCannotUpdateTest() {
        user1.setUserId(42L);

        Assertions
                .assertThatThrownBy(() -> this.repo.save(user1))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void getUsernameTest() {
        Assertions
                .assertThat(user1.getUsername())
                .isEqualTo(netId);
    }


    @Test
    void setUsernameIsUpdatableTest() {
        user1.setUsername("hahahahahaha@student.tudelft.nl");
        user1 = this.repo.save(user1);

        Assertions
                .assertThat(user1.getUsername())
                .isEqualTo("hahahahahaha@student.tudelft.nl");
    }


    @Test
    void setUsernameMustBeUniqTest() {

        Assertions
                .assertThatThrownBy(() ->
                        this.repo.save(new User(netId, "amogus", "sus", UserRole.STUDENT)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void getFirstNameTest() {
        Assertions
                .assertThat(user1.getFirstName())
                .isEqualTo("Steve");
    }

    @Test
    void setFirstNameTest() {
        user1.setFirstName("Val");

        Assertions
                .assertThat(user1.getFirstName())
                .isEqualTo("Val");
    }

    @Test
    void getLastNameTest() {
        Assertions
                .assertThat(user1.getLastName())
                .isEqualTo("Tudent");
    }

    @Test
    void setLastNameTest() {
        user1.setLastName("Python");

        Assertions
                .assertThat(user1.getLastName())
                .isEqualTo("Python");
    }

    @Test
    void getRoleTest() {
        Assertions
                .assertThat(user1.getRole())
                .isEqualTo(UserRole.STUDENT);
    }

    @Test
    void setRoleTest() {
        user1.setRole(UserRole.LECTURER);

        Assertions
                .assertThat(user1.getRole())
                .isEqualTo(UserRole.LECTURER);
    }

    @Test
    void testEqualsSameTest() {
        Assertions
                .assertThat(user1)
                .isEqualTo(user1);
    }

    @Test
    void testEqualsEqualTest() {
        User user2 = new User("ddelft@tudelft.nl", "delta", "delft", UserRole.LECTURER);
        user2.setUserId(user1.getUserId());

        Assertions
                .assertThat(user1)
                .isEqualTo(user2);
    }

    @Test
    void testEqualsDifferentClassTest() {
        Assertions
                .assertThat(user1)
                .isNotEqualTo(Long.valueOf(user1.getUserId()).toString());
    }

    @Test
    void testEqualsDifferentTest() {
        User user2 = new User("ddelft@tudelft.nl", "delta", "delft", UserRole.LECTURER);
        user2 = this.repo.save(user2);

        Assertions
                .assertThat(user1)
                .isNotEqualTo(user2);

        this.repo.delete(user2);
    }

    @Test
    void testEqualsDifferentButSameNetIdTest() {
        User user2 = new User("ddelft@tudelft.nl", "delta", "delft", UserRole.LECTURER);
        user2 = this.repo.save(user2);
        user2.setUsername(netId);

        Assertions
                .assertThat(user1)
                .isNotEqualTo(user2);

        this.repo.deleteById(user2.getUserId());
    }

    @Test
    void testHashCodeSameTest() {
        User user2 = new User("ddos@tudelft.nl", "d", "dos", UserRole.STUDENT);
        user2.setUserId(user1.getUserId());

        Assertions
                .assertThat(user2.hashCode())
                .isEqualTo(user1.hashCode());
    }

    @Test
    void testHashCodeDifferentTest() {
        User user2 = new User("S" + netId, user1.getFirstName(),
                user1.getLastName(), UserRole.STUDENT);
        this.repo.save(user2);

        Assertions
                .assertThat(user2.hashCode())
                .isNotEqualTo(user1.hashCode());

        this.repo.delete(user2);
    }
}