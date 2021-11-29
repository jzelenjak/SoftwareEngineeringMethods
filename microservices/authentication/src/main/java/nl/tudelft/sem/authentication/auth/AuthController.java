package nl.tudelft.sem.authentication.auth;

import nl.tudelft.sem.authentication.repository.UserDataRepository;
import nl.tudelft.sem.authentication.security.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("auth")
public class AuthController {
    private final transient AuthService authService;
    private final transient UserDataRepository users;
    private final transient PasswordEncoder passwordEncoder;

    /**
     * Controls the authentication.
     *
     * @param authService     the authentication service.
     * @param users           the users.
     * @param passwordEncoder the password encoder.
     */
    @Autowired
    public AuthController(AuthService authService, UserDataRepository users,
                          PasswordEncoder passwordEncoder) {
        this.authService = authService;
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new user to the system, if not already.
     *
     * @param username  the username of the user, which needs to be a valid NetID.
     * @param password  the password of the user.
     * @return true if successful.
     * @throws IllegalStateException if the user already exists.
     */
    @PostMapping("/register/{username}/{password}")
    public boolean register(@PathVariable("username") String username,
                        @PathVariable("password") String password) {
        try {
            UserRole role = UserRole.STUDENT;
            UserData user = new UserData(username,
                    passwordEncoder.encode(password), role);
            this.users.save(user);
            return true;
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException(String.format("User with username %s already exists.",
                    username));
        }
    }

    /* TODO Add API:
        Modify credentials (should/could) -> requires user to be authenticated already*/

    /**
     * PLACEHOLDER JAVADOC.
     * Allows the user to change their own credentials if authorized.
     *
     * @param username      the username to change.
     * @param password      the password to change.
     * @param newUsername   the new username.
     * @param newPassword   the new password.
     *
     * @return -1 for now.
     */
    @PutMapping("/change_credentials/{username}/{password}/{new_username}/{new_password}")
    public int changeCredentials(@PathVariable("username") String username,
                                 @PathVariable("password") String password,
                                 @PathVariable("new_username") String newUsername,
                                 @PathVariable("new_password") String newPassword) {
        // Authenticate the user
        // If successful, change the credentials
        return -1;
    }


    @GetMapping("/authenticate/{username}/{password}")
    public int authenticate(@PathVariable("username") String username,
                            @PathVariable("password") String password) {
        //
        return -1;
    }

    /* TODO Add API if necessary:
        Check validity <-> combine this with permission to return the role info directly */
    @GetMapping("/authenticate/{token}")
    public int validate(@PathVariable("token") String token) {
        // Validate the token
        return -1;
    }
}
