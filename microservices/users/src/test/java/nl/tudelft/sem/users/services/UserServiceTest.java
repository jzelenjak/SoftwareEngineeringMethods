package nl.tudelft.sem.users.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import nl.tudelft.sem.users.entities.User;
import nl.tudelft.sem.users.entities.UserRole;
import nl.tudelft.sem.users.repositories.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class UserServiceTest {
    @Autowired
    private transient UserRepository userRepository;

    @Autowired
    private transient UserService userService;

    private final transient String username = "amogus@student.tudelft.nl";
    private final transient String firstName = "Stan";
    private final transient String lastName = "Lee";

    /**
     * Tests for registerUser method.
     */

    @Test
    void testRegisterUserUsernameNull() {
        Assertions
            .assertThatThrownBy(() ->
                    this.userService.registerUser(null, "Amogus", "Amogusson"))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void testRegisterUserUsernameBlank() {
        Assertions
            .assertThatThrownBy(() ->
                    userService.registerUser("    ", "Amogus", "Amogussen"))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void testRegisterUserUsernameAlreadyExists() {
        this.userRepository.save(new User(username, "a", "mogus", UserRole.STUDENT));

        Assertions
            .assertThatThrownBy(() -> userService.registerUser(username, "amo", "gus"))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void testRegisterUserFirstNameNull() {
        Assertions
            .assertThatThrownBy(() -> this.userService.registerUser(username, null, "ogus"))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void testRegisterUserFirstNameBlank() {
        Assertions
            .assertThatThrownBy(() -> this.userService.registerUser(username, "", "ogus"))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void testRegisterUserLastNameNull() {
        Assertions
            .assertThatThrownBy(() -> this.userService.registerUser(username, "amog", null))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void testRegisterUserLastNameBlank() {
        Assertions
            .assertThatThrownBy(() -> this.userService.registerUser(username, "amog", " "))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void testRegisterUserSuccessful() {
        long id = userService.registerUser("oh", "my", "God");
        Assertions.assertThat(id).isGreaterThan(0L);
    }


    /**
     * Tests for getUserByUsername method.
     */


    @Test
    void testGetUserByUsernameFound() {
        String username = "impostor@tudelft.nl";
        User userFromRepo = userRepository.save(new User(username, "i", "mpostor", UserRole.ADMIN));
        Optional<User> userOptional = userService.getUserByUsername(username);

        Assertions.assertThat(userOptional).isPresent().get().isEqualTo(userFromRepo);
    }

    @Test
    void testGetUserByUsernameNotFound() {
        Assertions.assertThat(userService.getUserByUsername("susimpostor@tudelft.nl")).isEmpty();
    }


    /**
     * Tests for getUserByUserId method.
     */

    @Test
    void testGetUserByUserIdFound() {
        User user = userRepository.save(new User(username, "bbb", "ccc", UserRole.STUDENT));
        long userId = user.getUserId();

        Assertions
            .assertThat(userService.getUserByUserId(userId)).isPresent().get().isEqualTo(user);
    }

    @Test
    void testGetUserByUserIdNotFound() {
        Optional<User> userOptional = userService.getUserByUserId(3412235L);

        Assertions.assertThat(userOptional).isEmpty();
    }


    /**
     * Tests for getUsersByRole method.
     */

    @Test
    void testGetUsersByRoleFound() {
        List<User> usersFromRepo = new ArrayList<>();
        usersFromRepo.add(userRepository.save(new User("u1", "f1", "l1", UserRole.LECTURER)));
        usersFromRepo.add(userRepository.save(new User("u2", "f2", "l2", UserRole.LECTURER)));
        usersFromRepo.add(userRepository.save(new User("u3", "f3", "l3", UserRole.LECTURER)));
        userRepository.save(new User("u4", "f4", "l4", UserRole.ADMIN));

        Assertions
            .assertThat(userService.getUsersByRole(UserRole.LECTURER)).isEqualTo(usersFromRepo);
    }

    @Test
    void testGetUsersByRoleNotFound() {
        Assertions.assertThat(userService.getUsersByRole(UserRole.ADMIN)).isEmpty();
    }


    /**
     * Tests for changeRole method.
     */

    @Test
    void testChangeRoleUserNotFound() {
        Assertions
            .assertThat(userService.changeRole(63452L, UserRole.STUDENT))
            .isFalse();
    }

    @Test
    void testChangeRoleSuccessful() {
        User user = userRepository.save(new User(username, "ngl", "blob", UserRole.STUDENT));
        long userId = user.getUserId();

        Assertions
            .assertThat(userService.changeRole(userId, UserRole.LECTURER))
            .isTrue();
        Optional<User> savedUser = userService.getUserByUserId(userId);
        Assertions.assertThat(savedUser).isPresent();
        Assertions.assertThat(savedUser.get().getRole()).isEqualTo(UserRole.LECTURER);
    }

    /**
     * Tests for changeFirstName method.
     */

    @Test
    void testChangeFirstNameUserNotFound() {
        Assertions
                .assertThat(userService.changeFirstName(63452L, firstName))
                .isFalse();
    }

    @Test
    void testChangeFirstNameSuccessful() {
        User user = userRepository.save(new User(username, "Sad", lastName, UserRole.STUDENT));
        long userId = user.getUserId();

        Assertions
                .assertThat(userService.changeFirstName(userId, firstName))
                .isTrue();
        Optional<User> savedUser = userService.getUserByUserId(userId);
        Assertions.assertThat(savedUser).isPresent();
        Assertions.assertThat(savedUser.get().getFirstName()).isEqualTo(firstName);
    }

    /**
     * Tests for changeLastName method.
     */

    @Test
    void testChangeLastNameUserNotFound() {
        Assertions
                .assertThat(userService.changeLastName(63452L, lastName))
                .isFalse();
    }

    @Test
    void testChangeLastNameSuccessful() {
        User user = userRepository.save(new User(username, firstName, "Lie", UserRole.STUDENT));
        long userId = user.getUserId();

        Assertions
                .assertThat(userService.changeLastName(userId, lastName))
                .isTrue();
        Optional<User> savedUser = userService.getUserByUserId(userId);
        Assertions.assertThat(savedUser).isPresent();
        Assertions.assertThat(savedUser.get().getLastName()).isEqualTo(lastName);
    }

    /**
     * Tests for deleteUserByUserId method.
     */

    @Test
    void testDeleteUserByUserIdUserNotFound() {
        Assertions.assertThat(userService.deleteUserByUserId(4242442L)).isFalse();
    }

    @Test
    void testDeleteUserByUserIdSuccessful() {
        User user = userRepository.save(new User("blob@tudelft.nl", "b", "lob", UserRole.STUDENT));
        long userId = user.getUserId();

        Assertions.assertThat(userService.deleteUserByUserId(userId)).isTrue();
        Assertions.assertThat(userService.getUserByUserId(userId)).isEmpty();
    }
}