package nl.tudelft.sem.users.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import nl.tudelft.sem.users.entities.User;
import nl.tudelft.sem.users.entities.UserRole;
import nl.tudelft.sem.users.repositories.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
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

    private final transient String netId = "amogus@student.tudelft.nl";

    /**
     * Tests for registerUser method.
     */

    @Test
    void registerUserNetIdNullTest() {
        Assertions
                .assertThatThrownBy(() ->
                        this.userService.registerUser(null, "Amogus", "Amogusson"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void registerUserNetIdBlankTest() {
        Assertions
                .assertThatThrownBy(() ->
                        userService.registerUser("    ", "Amogus", "Amogussen"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void registerUserNetIdAlreadyExistsTest() {
        this.userRepository.save(new User(netId, "a", "mogus", UserRole.TA));

        Assertions
                .assertThatThrownBy(() -> userService.registerUser(netId, "amo", "gus"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void registerUserFirstNameNullTest() {
        Assertions
                .assertThatThrownBy(() -> this.userService.registerUser(netId, null, "ogus"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void registerUserFirstNameBlankTest() {
        Assertions
                .assertThatThrownBy(() -> this.userService.registerUser(netId, "", "ogus"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void registerUserLastNameNullTest() {
        Assertions
                .assertThatThrownBy(() -> this.userService.registerUser(netId, "amog", null))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void registerUserLastNameBlankTest() {
        Assertions
                .assertThatThrownBy(() -> this.userService.registerUser(netId, "amog", " "))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void registerUserSuccessfulTest() {
        long id = userService.registerUser("oh", "my", "God");
        Assertions.assertThat(id).isGreaterThan(0L);
    }


    /**
     * Tests for getUserByNetId method.
     */


    @Test
    void getUserByNetIdFoundTest() {
        String netId = "impostor@tudelft.nl";
        User userFromRepo = userRepository.save(new User(netId, "i", "mpostor", UserRole.ADMIN));
        Optional<User> userOptional = userService.getUserByNetId(netId);

        Assertions.assertThat(userOptional).isPresent().get().isEqualTo(userFromRepo);
    }

    @Test
    void getUserByNetIdNotFoundTest() {
        Assertions.assertThat(userService.getUserByNetId("susimpostor@tudelft.nl")).isEmpty();
    }


    /**
     * Tests for getUserByUserId method.
     */

    @Test
    void getUserByUserIdFoundTest() {
        User user = userRepository.save(new User("aaa", "bbb", "ccc", UserRole.STUDENT));
        long userId = user.getUserId();

        Assertions
                .assertThat(userService.getUserByUserId(userId)).isPresent().get().isEqualTo(user);
    }

    @Test
    void getUserByUserIdNotFoundTest() {
        Optional<User> userOptional = userService.getUserByUserId(3412235L);

        Assertions.assertThat(userOptional).isEmpty();
    }


    /**
     * Tests for getUsersByRole method.
     */

    @Test
    void getUsersByRoleFoundTest() {
        List<User> usersFromRepo = new ArrayList<>();
        usersFromRepo.add(userRepository.save(new User("u1", "f1", "l1", UserRole.LECTURER)));
        usersFromRepo.add(userRepository.save(new User("u2", "f2", "l2", UserRole.LECTURER)));
        usersFromRepo.add(userRepository.save(new User("u3", "f3", "l3", UserRole.LECTURER)));
        userRepository.save(new User("u4", "f4", "l4", UserRole.ADMIN));

        Assertions
                .assertThat(userService.getUsersByRole(UserRole.LECTURER)).isEqualTo(usersFromRepo);
    }

    @Test
    void getUsersByRoleNotFoundTest() {
        Assertions.assertThat(userService.getUsersByRole(UserRole.ADMIN)).isEmpty();
    }


    /**
     * Tests for changeRole method.
     */

    @Test
    void changeRoleOnlyAdminsAndLecturersCanChangeRolesTest() {
        User user = userRepository.save(new User(netId, "p", "y", UserRole.TA));
        long userId = user.getUserId();

        Assertions
                .assertThat(userService.changeRole(userId, UserRole.STUDENT, UserRole.TA))
                .isFalse();
        Optional<User> savedUser = userService.getUserByUserId(userId);
        Assertions.assertThat(savedUser).isPresent();
        Assertions.assertThat(savedUser.get().getRole()).isEqualTo(UserRole.TA);
    }

    @Test
    void changeRoleOnlyAdminCanMakeOthersAnAdminFalseTest() {
        User user = userRepository.save(new User(netId, "m", "s", UserRole.TA));
        long userId = user.getUserId();

        Assertions
                .assertThat(userService.changeRole(userId, UserRole.ADMIN, UserRole.LECTURER))
                .isFalse();
        Optional<User> savedUser = userService.getUserByUserId(userId);
        Assertions.assertThat(savedUser).isPresent();
        Assertions.assertThat(savedUser.get().getRole()).isEqualTo(UserRole.TA);
    }

    @Test
    void changeRoleOnlyAdminCanMakeOthersAnAdminTrueTest() {
        User user = userRepository.save(new User(netId, "add", "ddd", UserRole.LECTURER));
        long userId = user.getUserId();

        Assertions
                .assertThat(userService.changeRole(userId, UserRole.ADMIN, UserRole.ADMIN))
                .isTrue();

        Optional<User> savedUser = userService.getUserByUserId(user.getUserId());
        Assertions.assertThat(savedUser).isPresent();
        Assertions.assertThat(savedUser.get().getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    void changeRoleOnlyAdminCanMakeOthersA_LecturerFalseTest() {
        User user = userRepository.save(new User(netId, "mammm", "omgus", UserRole.TA));
        long userId = user.getUserId();

        Assertions
                .assertThat(userService.changeRole(userId, UserRole.LECTURER, UserRole.LECTURER))
                .isFalse();
        Optional<User> savedUser = userService.getUserByUserId(userId);
        Assertions.assertThat(savedUser).isPresent();
        Assertions.assertThat(savedUser.get().getRole()).isEqualTo(UserRole.TA);
    }

    @Test
    void changeRoleOnlyAdminCanMakeOthersA_LecturerTrueTest() {
        User user = userRepository.save(new User(netId, "ammm", "ogggus", UserRole.TA));
        long userId = user.getUserId();

        Assertions
                .assertThat(userService.changeRole(userId, UserRole.LECTURER, UserRole.ADMIN))
                .isTrue();

        Optional<User> savedUser = userService.getUserByUserId(userId);
        Assertions.assertThat(savedUser).isPresent();
        Assertions.assertThat(savedUser.get().getRole()).isEqualTo(UserRole.LECTURER);
    }

    @Test
    void changeRoleOnlyAdminCanDowngradeAnotherAdminFalseTest() {
        User user = userRepository.save(new User(netId, "amogu", "s", UserRole.ADMIN));
        long userId = user.getUserId();

        Assertions
                .assertThat(userService.changeRole(userId, UserRole.STUDENT, UserRole.LECTURER))
                .isFalse();
        Optional<User> savedUser = userService.getUserByUserId(userId);
        Assertions.assertThat(savedUser).isPresent();
        Assertions.assertThat(savedUser.get().getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    void changeRoleOnlyAdminCanDowngradeAnotherAdminTrueTest() {
        User user = userRepository.save(new User(netId, "amoguu", "sss", UserRole.ADMIN));
        long userId = user.getUserId();

        Assertions
                .assertThat(userService.changeRole(userId, UserRole.STUDENT, UserRole.ADMIN))
                .isTrue();

        Optional<User> savedUser = userService.getUserByUserId(userId);
        Assertions.assertThat(savedUser).isPresent();
        Assertions.assertThat(savedUser.get().getRole()).isEqualTo(UserRole.STUDENT);

    }

    @Test
    void changeRoleUserNotFoundTest() {
        Assertions
                .assertThat(userService.changeRole(63452L, UserRole.STUDENT, UserRole.ADMIN))
                .isFalse();
    }

    @Test
    void changeRoleFromStudentToTaSuccessfulTest() {
        User user = userRepository.save(new User(netId, "ngl", "blob", UserRole.STUDENT));
        long userId = user.getUserId();

        Assertions
                .assertThat(userService.changeRole(userId, UserRole.TA, UserRole.LECTURER))
                .isTrue();
        Optional<User> savedUser = userService.getUserByUserId(userId);
        Assertions.assertThat(savedUser).isPresent();
        Assertions.assertThat(savedUser.get().getRole()).isEqualTo(UserRole.TA);
    }

    @Test
    void changeRoleFromAdminToStudentFailureTest() {
        User user = userRepository.save(new User(netId, "nglb", "ob", UserRole.ADMIN));
        long userId = user.getUserId();

        Assertions
                .assertThat(userService.changeRole(userId, UserRole.STUDENT, UserRole.LECTURER))
                .isFalse();
        Optional<User> savedUser = userService.getUserByUserId(userId);
        Assertions.assertThat(savedUser).isPresent();
        Assertions.assertThat(savedUser.get().getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    void changeRoleFromStudentToAdminFailureTest() {
        User user = userRepository.save(new User(netId, "b", "lob", UserRole.STUDENT));
        long userId = user.getUserId();

        Assertions
                .assertThat(userService.changeRole(userId, UserRole.ADMIN, UserRole.LECTURER))
                .isFalse();
        Optional<User> savedUser = userService.getUserByUserId(userId);
        Assertions.assertThat(savedUser).isPresent();
        Assertions.assertThat(savedUser.get().getRole()).isEqualTo(UserRole.STUDENT);
    }

    /**
     * Tests for deleteUserByUserId method.
     */


    @Test
    void deleteUserByUserIdNotAdminTest() {
        User user = userRepository.save(new User("4@tudelft.nl", "bj", "lb", UserRole.STUDENT));
        long userId = user.getUserId();

        Assertions.assertThat(userService.deleteUserByUserId(userId, UserRole.LECTURER)).isFalse();
        Assertions.assertThat(userService.getUserByUserId(userId)).isPresent();
    }

    @Test
    void deleteUserByUserIdSuccessfulTest() {
        User user = userRepository.save(new User("blob@tudelft.nl", "b", "lob", UserRole.STUDENT));
        long userId = user.getUserId();

        Assertions.assertThat(userService.deleteUserByUserId(userId, UserRole.ADMIN)).isTrue();
        Assertions.assertThat(userService.getUserByUserId(userId)).isEmpty();
    }
}