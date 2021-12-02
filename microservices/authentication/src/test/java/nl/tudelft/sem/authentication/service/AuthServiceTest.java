package nl.tudelft.sem.authentication.service;

import nl.tudelft.sem.authentication.entities.UserData;
import nl.tudelft.sem.authentication.repositories.UserDataRepository;
import nl.tudelft.sem.authentication.security.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

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

        assertFalse(this.authService.registerUser("jegor", "password2"),
                "The user must not have been registered");

        this.userDataRepository.deleteById("jegor");
    }

    @Test
    void registerUserNotYesExistsTest() {
        assertTrue(this.authService.registerUser("impostor", "password2"),
                "The user must have been registered");
        assertTrue(this.userDataRepository.findByUsername("impostor").isPresent(),
                "The user must have been loaded");
        assertEquals(UserRole.STUDENT,
                this.userDataRepository.findByUsername("impostor").get().getRole(),
                "The role of the user must not have been changed");

        this.userDataRepository.deleteById("impostor");
    }

    @Test
    void loadUserNotFoundTest() {
        assertThrows(UsernameNotFoundException.class,
                () -> this.authService.loadUserByUsername("jegor"),
                "The user must not have been loaded from the repository");
    }

    @Test
    void changePasswordTest() {
        this.userDataRepository.save(new UserData("red_kinda_sus_ngl",
                this.passwordEncoder.encode("sus"), UserRole.TA));

        this.authService.changePassword("red_kinda_sus_ngl", "ngl");

        assertTrue(this.userDataRepository.findByUsername("red_kinda_sus_ngl").isPresent(),
                "The user must be present in the repository");
        assertTrue(this.passwordEncoder.matches("ngl",
                this.userDataRepository.findByUsername("red_kinda_sus_ngl").get().getPassword()),
                "The new password must be hashed (encoded)");
        assertEquals(UserRole.TA,this.userDataRepository.findByUsername("red_kinda_sus_ngl")
                .get().getRole(), "The role of the user must not have been changed");

        this.userDataRepository.deleteById("red_kinda_sus_ngl");
    }
}