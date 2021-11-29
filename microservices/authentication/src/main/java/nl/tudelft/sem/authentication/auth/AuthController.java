package nl.tudelft.sem.authentication.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("auth")
public class AuthController {
    private transient AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /*TODO Add API:
       Register user credentials (must, can be done once, if there's an entry -> not allowed)*/
    @PostMapping("/register/{username}/{password}")
    public int register(@PathVariable("username") String username,
                        @PathVariable("password") String password) {
        // Register
        return -1;
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
