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
import org.springframework.dao.DataIntegrityViolationException;

class UserServiceTest {
    private transient UserRepository userRepository;

    private transient UserService userService;

    private final transient String netId = "amogus@student.tudelft.nl";
    private final transient long userId = 4558965L;
    private final transient User user = new User(netId, "a", "mogus", UserRole.TA);

    @Mock
    private transient User userMock;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        userService = new UserService(userRepository);
    }

    /**
     * A helper method to give Mockito rules for mocking findByUsername method.
     *
     * @param username      the username of the user
     * @param userToReturn  the user that is to be returned
     */
    private void mockFindByUsername(String username, User userToReturn) {
        if (userToReturn == null) {
            Mockito
                .when(userRepository.findByUsername(username))
                .thenReturn(Optional.empty());
        } else {
            Mockito
                .when(userRepository.findByUsername(username))
                .thenReturn(Optional.of(userToReturn));
        }
    }

    /**
     * A helper method to verify that a mock has been called for findByUsername method.
     *
     * @param username      the username with which the mock is expected to have been called
     */
    private void verifyFindByUsername(String username) {
        Mockito
            .verify(userRepository, Mockito.times(1))
            .findByUsername(username);
    }


    /**
     * A helper method to give Mockito rules for mocking save method.
     *
     * @param userToSave      the user to be saved
     * @param userToReturn    the user to be returned
     */
    private void mockSave(User userToSave, User userToReturn) {
        Mockito
            .when(userRepository.save(userToSave))
            .thenReturn(userToReturn);
    }

    /**
     * A helper method to verify that a mock has been called for save method.
     *
     * @param user          the user with which the mock is expected to have been called
     * @param expectedTimes the number of times the mock is expected to have been called
     */
    private void verifySave(User user, int expectedTimes) {
        Mockito
            .verify(userRepository, Mockito.times(expectedTimes))
            .save(user);
    }


    /**
     * A helper method to give Mockito rules for mocking findByUserId method.
     *
     * @param userId        the user ID of the user
     * @param userToReturn  the user that is to be returned
     */
    private void mockFindByUserId(long userId, User userToReturn) {
        if (userToReturn == null) {
            Mockito
                .when(userRepository.findByUserId(userId))
                .thenReturn(Optional.empty());
        } else {
            Mockito
                .when(userRepository.findByUserId(userId))
                .thenReturn(Optional.of(userToReturn));
        }
    }

    /**
     * A helper method to verify that a mock has been called for findByUserId method.
     *
     * @param userId the user ID with which the mock is expected to have been called
     */
    private void verifyFindByUserId(long userId) {
        Mockito
            .verify(userRepository, Mockito.times(1))
            .findByUserId(userId);
    }


    /**
     * Tests for registerUser method.
     */

    @Test
    void registerUserNetIdNullTest() {
        Assertions
            .assertThatThrownBy(() ->
                this.userService.registerUser(null, "Amogus", "Amogusson"))
            .isInstanceOf(DataIntegrityViolationException.class);

        verifySave(userMock, 0);
    }

    @Test
    void registerUserNetIdBlankTest() {
        Assertions
            .assertThatThrownBy(() ->
                userService.registerUser("    ", "Amogus", "Amogussen"))
            .isInstanceOf(DataIntegrityViolationException.class);

        verifySave(userMock, 0);
    }

    @Test
    void registerUserNetIdEmptyTest() {
        Assertions
            .assertThatThrownBy(() ->
                userService.registerUser("", "Amogus", "Amogussan"))
            .isInstanceOf(DataIntegrityViolationException.class);

        verifySave(userMock, 0);
    }

    @Test
    void registerUserNetIdAlreadyExistsTest() {
        mockFindByUsername(netId, user);

        Assertions
            .assertThatThrownBy(() ->
                userService.registerUser(netId, "amo", "gus"))
            .isInstanceOf(DataIntegrityViolationException.class);

        verifyFindByUsername(netId);
        verifySave(userMock, 0);
    }

    @Test
    void registerUserFirstNameNullTest() {
        mockFindByUsername(netId, null);

        Assertions
            .assertThatThrownBy(() ->
                this.userService.registerUser(netId, null, "ogus"))
            .isInstanceOf(DataIntegrityViolationException.class);

        verifyFindByUsername(netId);
        verifySave(userMock, 0);
    }

    @Test
    void registerUserFirstNameEmptyTest() {
        mockFindByUsername(netId, null);

        Assertions
            .assertThatThrownBy(() ->
                this.userService.registerUser(netId, "", "ogus"))
            .isInstanceOf(DataIntegrityViolationException.class);

        verifyFindByUsername(netId);
        verifySave(userMock, 0);
    }

    @Test
    void registerUserFirstNameBlankTest() {
        mockFindByUsername(netId, null);

        Assertions
            .assertThatThrownBy(() ->
                this.userService.registerUser(netId, "      ", "ogus"))
            .isInstanceOf(DataIntegrityViolationException.class);

        verifyFindByUsername(netId);
        verifySave(userMock, 0);
    }

    @Test
    void registerUserLastNameNullTest() {
        mockFindByUsername(netId, null);

        Assertions
                .assertThatThrownBy(() ->
                        this.userService.registerUser(netId, "amog", null))
                .isInstanceOf(DataIntegrityViolationException.class);

        verifyFindByUsername(netId);
        verifySave(userMock, 0);
    }

    @Test
    void registerUserLastNameEmptyTest() {
        mockFindByUsername(netId, null);

        Assertions
                .assertThatThrownBy(() ->
                        this.userService.registerUser(netId, "amog", ""))
                .isInstanceOf(DataIntegrityViolationException.class);

        verifyFindByUsername(netId);
        verifySave(userMock, 0);
    }

    @Test
    void registerUserLastNameBlankTest() {
        mockFindByUsername(netId, null);

        Assertions
                .assertThatThrownBy(() ->
                        this.userService.registerUser(netId, "amog", "    "))
                .isInstanceOf(DataIntegrityViolationException.class);

        verifyFindByUsername(netId);
        verifySave(userMock, 0);
    }

    @Test
    void registerUserSuccessfulTest() {
        User savedUser =
                new User(user.getUsername(), user.getFirstName(), user.getLastName(), UserRole.TA);
        savedUser.setUserId(3453211L);

        mockFindByUsername(netId, null);
        mockSave(user, savedUser);


        long id = userService
                .registerUser(user.getUsername(), user.getFirstName(), user.getLastName());

        Assertions
            .assertThat(id)
            .isEqualTo(3453211L);

        verifyFindByUsername(netId);
        verifySave(user, 1);
    }


    /**
     * Tests for getUserByNetId method.
     */


    @Test
    void getUserByNetIdFoundTest() {
        String netId = "impostor@tudelft.nl";
        User userFromRepo =
                new User(netId, "i", "mpostor", UserRole.ADMIN);
        userFromRepo.setUserId(5325965L);

        mockFindByUsername(netId, userFromRepo);

        Optional<User> userOptional = userService.getUserByNetId(netId);

        Assertions
            .assertThat(userOptional)
            .isPresent()
            .get()
            .isEqualTo(userFromRepo);

        verifyFindByUsername(netId);
    }

    @Test
    void getUserByNetIdNotFoundTest() {
        String netId = "susimpostor@tudelft.nl";

        mockFindByUsername(netId, null);

        Optional<User> userOptional = userService.getUserByNetId(netId);

        Assertions
                .assertThat(userOptional)
                .isEmpty();

        verifyFindByUsername(netId);
    }


    /**
     * Tests for getUserByUserId method.
     */

    @Test
    void getUserByUserIdFoundTest() {
        long userId = 4324235L;
        User userFromRepo =
                new User("notimpostor@tudelft.nl", "not", "impostor", UserRole.ADMIN);
        userFromRepo.setUserId(userId);

        mockFindByUserId(userId, userFromRepo);

        Optional<User> userOptional = userService.getUserByUserId(userId);

        Assertions
            .assertThat(userOptional)
            .isPresent()
            .get()
            .isEqualTo(userFromRepo);

        verifyFindByUserId(userId);
    }

    @Test
    void getUserByUserIdNotFoundTest() {
        long userId = 3412235L;

        mockFindByUserId(userId, null);

        Optional<User> userOptional = userService.getUserByUserId(userId);

        Assertions
            .assertThat(userOptional)
            .isEmpty();

        verifyFindByUserId(userId);
    }


    /**
     * Tests for getUsersByRole method.
     */

    @Test
    void getUsersByRoleFoundTest() {
        String lastName = "sus";
        List<User> usersFromRepo =
                List.of(
                    new User("redsus@tudelft.nl", "red", lastName, UserRole.LECTURER),
                    new User("greensus@tudelft.nl", "green", lastName, UserRole.LECTURER),
                    new User("bluesus@tudelft.nl", "blue", lastName, UserRole.LECTURER),
                    new User("yellowsus@tudelft.nl", "yellow", lastName, UserRole.LECTURER)
                );
        Mockito
            .when(userRepository.findAllByRole(UserRole.LECTURER))
            .thenReturn(usersFromRepo);

        List<User> users = userService.getUsersByRole(UserRole.LECTURER);

        Assertions
            .assertThat(users)
            .isEqualTo(usersFromRepo);

        Mockito
            .verify(userRepository, Mockito.times(1))
            .findAllByRole(UserRole.LECTURER);
    }

    @Test
    void getUsersByRoleNotFoundTest() {
        List<User> usersFromRepo = new ArrayList<>();

        Mockito
            .when(userRepository.findAllByRole(UserRole.ADMIN))
            .thenReturn(usersFromRepo);

        List<User> users = userService.getUsersByRole(UserRole.ADMIN);

        Assertions
            .assertThat(users)
            .isEmpty();

        Mockito
            .verify(userRepository, Mockito.times(1))
            .findAllByRole(UserRole.ADMIN);
    }


    /**
     * Tests for changeRole method.
     */

    @Test
    void changeRoleOnlyAdminsAndLecturersCanChangeRolesTest() {
        Assertions
            .assertThat(userService.changeRole(userId, UserRole.STUDENT, UserRole.TA))
            .isFalse();

        verifySave(userMock, 0);
    }

    @Test
    void changeRoleOnlyAdminCanMakeOthersAnAdminFalseTest() {
        Assertions
            .assertThat(userService.changeRole(userId, UserRole.ADMIN, UserRole.LECTURER))
            .isFalse();

        verifySave(userMock, 0);
    }

    @Test
    void changeRoleOnlyAdminCanMakeOthersAnAdminTrueTest() {
        User userFromRepo = new User(netId, "add", "ddd", UserRole.LECTURER);
        userFromRepo.setUserId(userId);

        User userSaved = new User(userFromRepo.getUsername(),
                userFromRepo.getFirstName(), userFromRepo.getLastName(), UserRole.CANDIDATE_TA);
        userSaved.setUserId(userId);

        mockFindByUserId(userId, userFromRepo);
        mockSave(userSaved, userSaved);

        Assertions
            .assertThat(userService.changeRole(userId, UserRole.ADMIN, UserRole.ADMIN))
            .isTrue();

        verifyFindByUserId(userId);
        verifySave(userSaved, 1);
    }

    @Test
    void changeRoleOnlyAdminCanMakeOthersA_LecturerFalseTest() {
        Assertions
            .assertThat(userService.changeRole(userId, UserRole.LECTURER, UserRole.LECTURER))
            .isFalse();

        verifySave(userMock, 0);
    }

    @Test
    void changeRoleOnlyAdminCanMakeOthersA_LecturerTrueTest() {
        User userFromRepo = new User(netId, "ammmmmm", "ogggggus", UserRole.TA);
        userFromRepo.setUserId(userId);

        User userSaved = new User(userFromRepo.getUsername(),
                userFromRepo.getFirstName(), userFromRepo.getLastName(), UserRole.LECTURER);
        userSaved.setUserId(userId);

        mockFindByUserId(userId, userFromRepo);
        mockSave(userSaved, userSaved);

        Assertions
            .assertThat(userService.changeRole(userId, UserRole.LECTURER, UserRole.ADMIN))
            .isTrue();

        verifySave(userSaved, 1);
        verifyFindByUserId(userId);
    }

    @Test
    void changeRoleOnlyAdminCanDowngradeAnotherAdminFalseTest() {
        UserRole requesterRole = UserRole.LECTURER;

        User userFromRepo = new User(netId, "amogu", "s", UserRole.ADMIN);
        userFromRepo.setUserId(userId);

        mockFindByUserId(userId, userFromRepo);

        Assertions
            .assertThat(userService.changeRole(userId, UserRole.STUDENT, requesterRole))
            .isFalse();

        verifyFindByUserId(userId);
        verifySave(userMock, 0);
    }

    @Test
    void changeRoleOnlyAdminCanDowngradeAnotherAdminTrueTest() {
        User userFromRepo = new User(netId, "amoguuuuu", "sssss", UserRole.ADMIN);
        userFromRepo.setUserId(userId);

        User userSaved = new User(userFromRepo.getUsername(), userFromRepo.getFirstName(),
                userFromRepo.getLastName(), UserRole.STUDENT);
        userSaved.setUserId(userId);

        mockFindByUserId(userId, userFromRepo);
        mockSave(userSaved, userSaved);

        Assertions
            .assertThat(userService.changeRole(userId, UserRole.STUDENT, UserRole.ADMIN))
            .isTrue();

        verifyFindByUserId(userId);
        verifySave(userSaved, 1);
    }

    @Test
    void changeRoleFromStudentToCandidateTaSuccessfulTest() {
        User userFromRepo = new User("nglblob@tudelft.nl", "ngl", "blob", UserRole.STUDENT);
        userFromRepo.setUserId(userId);

        User userSaved = new User("nglblob@tudelft.nl", "ngl", "blob", UserRole.CANDIDATE_TA);
        userSaved.setUserId(userId);

        mockFindByUserId(userId, userFromRepo);
        mockSave(userSaved, userSaved);

        Assertions
            .assertThat(userService.changeRole(userId, UserRole.CANDIDATE_TA, UserRole.ADMIN))
            .isTrue();

        verifyFindByUserId(userId);
        verifySave(userSaved, 1);
    }

    /**
     * Tests for deleteUserByUserId method.
     */


    @Test
    void deleteUserByUserIdNotAdminTest() {
        Assertions
            .assertThat(userService.deleteUserByUserId(userId, UserRole.LECTURER))
            .isFalse();
        Mockito
            .verify(userRepository, Mockito.times(0))
            .deleteByUserId(Mockito.anyLong());
    }

    @Test
    void deleteUserByUserIdSuccessfulTest() {
        Assertions
            .assertThat(userService.deleteUserByUserId(userId, UserRole.ADMIN))
            .isTrue();

        Mockito
            .verify(userRepository, Mockito.times(1))
            .deleteByUserId(userId);
    }
}