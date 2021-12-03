package nl.tudelft.sem.authentication.service;

import nl.tudelft.sem.authentication.entities.UserData;
import nl.tudelft.sem.authentication.repositories.UserDataRepository;
import nl.tudelft.sem.authentication.security.UserRole;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;


@SpringBootTest
class AuthServiceTest {
    @Autowired
    private transient AuthService authService;

    @Autowired
    private transient UserDataRepository userDataRepository;

    @Autowired
    private transient PasswordEncoder passwordEncoder;

    @Test
    void registerUserAlreadyExistsTest() {
        this.userDataRepository.save(new UserData("jegor",
                this.passwordEncoder.encode("amogus"), UserRole.TA));

        Assertions.assertFalse(this.authService.registerUser("jegor", "password2"),
                "The user must not have been registered");

        this.userDataRepository.deleteById("jegor");
    }

    @Test
    void registerUserNotYesExistsTest() {
        String username = "impostor";
        Assertions.assertTrue(this.authService.registerUser(username, "password2"),
                "The user must have been registered");
        Assertions.assertTrue(this.userDataRepository.findByUsername(username).isPresent(),
                "The user must have been loaded");
        Assertions.assertEquals(UserRole.STUDENT,
                this.userDataRepository.findByUsername(username).get().getRole(),
                "The role of the user must not have been changed");
        this.userDataRepository.deleteById(username);
    }

    @Test
    void loadUserNotFoundTest() {
        Assertions.assertThrows(UsernameNotFoundException.class,
                () -> this.authService.loadUserByUsername("jegorka"),
                "The user must not have been loaded from the repository");
    }

    @Test
    void changePasswordTest() {
        String username = "red_kinda_sus_ngl";
        this.userDataRepository.save(new UserData(username,
                this.passwordEncoder.encode("sus"), UserRole.TA));

        this.authService.changePassword(username, "ngl");

        Assertions.assertTrue(this.userDataRepository.findByUsername(username).isPresent(),
                "The user must be present in the repository");
        Assertions.assertTrue(this.passwordEncoder.matches("ngl",
                this.userDataRepository.findByUsername(username).get().getPassword()),
                "The new password must be hashed (encoded)");
        Assertions.assertEquals(UserRole.TA, this.userDataRepository.findByUsername(username)
                .get().getRole(), "The role of the user must not have been changed");
        this.userDataRepository.deleteById(username);
    }
}