package nl.tudelft.sem.authentication.service;

import java.util.Optional;
import nl.tudelft.sem.authentication.entities.UserData;
import nl.tudelft.sem.authentication.repositories.UserDataRepository;
import nl.tudelft.sem.authentication.security.UserRole;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;


@SpringBootTest
class AuthServiceTest {

    @Autowired
    private transient AuthService authService;

    @Value("${root.username}")
    private transient String rootUsername;

    @Value("${root.password}")
    private transient String rootPassword;

    @Value("${root.userid}")
    private transient long rootUserId;

    @Autowired
    private transient UserDataRepository userDataRepository;

    @Autowired
    private transient PasswordEncoder passwordEncoder;


    private String encode(String password) {
        return this.passwordEncoder.encode(password);
    }

    @Test
    void testRegisterRootUserNotRegistered() {
        UserDataRepository repo = Mockito.mock(UserDataRepository.class);
        UserData root = new UserData(rootUsername, passwordEncoder.encode(rootPassword),
                            UserRole.ADMIN, rootUserId);
        Mockito
            .when(repo.findByUsername(rootUsername))
            .thenReturn(Optional.empty());
        Mockito
            .when(repo.save(root))
            .thenReturn(root);

        new AuthService(repo, passwordEncoder, rootUsername, rootPassword, rootUserId);

        Mockito
            .verify(repo, Mockito.times(1))
            .findByUsername(rootUsername);
        Mockito
            .verify(repo, Mockito.times(1))
            .save(root);
    }

    @Test
    void testRegisterRootUserAlreadyRegistered() {
        UserDataRepository repo = Mockito.mock(UserDataRepository.class);
        UserData root = new UserData(rootUsername, passwordEncoder.encode(rootPassword),
                UserRole.ADMIN, rootUserId);
        Mockito
                .when(repo.findByUsername(rootUsername))
                .thenReturn(Optional.of(root));

        new AuthService(repo, passwordEncoder, rootUsername, rootPassword, rootUserId);

        Mockito
                .verify(repo, Mockito.times(1))
                .findByUsername(rootUsername);
        Mockito
                .verify(repo, Mockito.times(0))
                .save(root);
    }

    @Test
    void testRegisterUserAlreadyExistsByUsername() {
        this.userDataRepository
                .save(new UserData("Andy", encode("amogus"), UserRole.STUDENT, 3957639L));

        Assertions.assertFalse(this.authService.registerUser("Andy", 4957639L, "password2"),
                "The user must not have been registered");

        this.userDataRepository.deleteById("Andy");
    }

    @Test
    void testRegisterUserAlreadyExistsByUserId() {
        this.userDataRepository
                .save(new UserData("Jegor", encode("amogus"), UserRole.STUDENT, 8105739L));

        Assertions.assertFalse(this.authService.registerUser("andy", 8105739L, "password2"),
                "The user must not have been registered");

        this.userDataRepository.deleteById("Jegor");
    }

    @Test
    void testRegisterNewUser() {
        String username = "impostor";

        Assertions.assertTrue(this.authService.registerUser(username, 7803850L, "password2"),
                "The user must have been registered");
        Assertions.assertTrue(this.userDataRepository.findByUsername(username).isPresent(),
                "The user must have been loaded");
        Assertions.assertEquals(UserRole.STUDENT,
                this.userDataRepository.findByUsername(username).get().getRole(),
                "The role of the user must not have been changed");

        this.userDataRepository.deleteById(username);
    }

    @Test
    void testLoadUserByUsernameNotFound() {
        Assertions.assertThrows(UsernameNotFoundException.class,
                () -> this.authService.loadUserByUsername("jegorka"),
                "The user must not have been loaded from the repository");
    }

    @Test
    void testLoadUserByUsernameFound() {
        UserData user = new UserData("GNU", encode("GNU/LINUX"), UserRole.STUDENT, 3452341L);
        this.userDataRepository.save(user);

        Assertions.assertEquals(user, this.authService.loadUserByUsername("GNU"),
                "The user must have been loaded from the repository");

        this.userDataRepository.deleteById("GNU");
    }

    @Test
    void testLoadUserByUserIdNotFound() {
        Assertions.assertThrows(UsernameNotFoundException.class,
                () -> this.authService.loadUserByUserId(4242442L),
                "The user must not have been loaded from the repository");
    }

    @Test
    void testLoadUserByUserIdFound() {
        UserData user = new UserData("gosha", encode("myfirendamogus"),
                UserRole.STUDENT, 5327639L);
        this.userDataRepository.save(user);

        Assertions.assertEquals(user, this.authService.loadUserByUserId(5327639L),
                "The user must have been loaded from the repository");

        this.userDataRepository.deleteById("gosha");
    }

    @Test
    void testChangePasswordSuccess() {
        String username = "red_kinda_sus_ngl";
        this.userDataRepository
                .save(new UserData(username, encode("sus"), UserRole.STUDENT, 3425101L));

        this.authService.changePassword(username, "ngl");

        Assertions.assertTrue(this.userDataRepository.findByUsername(username).isPresent(),
                "The user must be present in the repository");
        Assertions.assertTrue(this.passwordEncoder.matches("ngl",
                this.userDataRepository.findByUsername(username).get().getPassword()),
                "The new password must be hashed (encoded)");
        Assertions.assertEquals(UserRole.STUDENT, this.userDataRepository.findByUsername(username)
                .get().getRole(), "The role of the user must not have been changed");
        this.userDataRepository.deleteById(username);
    }
}