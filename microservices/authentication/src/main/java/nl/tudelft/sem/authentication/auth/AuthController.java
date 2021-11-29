package nl.tudelft.sem.authentication.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("auth")
public class AuthController {
    private transient AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    //TODO Add API: Register user credentials (must, can be done once, if there's an entry -> not allowed)
    @PostMapping("/resigter/{username}/{password}")
    public int register(@PathVariable("username") String username, @PathVariable("password") String password) {
        // Register
        return -1;
    }

    // TODO: Add API: Modify credentials (should/could) -> requires user to be authenticated already
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
    public int authenticate(@PathVariable("username") String username, @PathVariable("password") String password) {
        //
        return -1;
    }

    // TODO Add API if necessary: Check validity <-> combine this with permission to return the role info directly
    @GetMapping("/authenticate/{token}")
    public int validate(@PathVariable("token") String token) {
        // Validate the token
        return -1;
    }
}
