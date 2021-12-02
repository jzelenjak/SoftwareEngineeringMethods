package nl.tudelft.sem.entities.controllers;

import nl.tudelft.sem.entities.services.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controller class for users.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final transient UserService userService;


    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public void registerUser(HttpServletRequest req, HttpServletResponse res) {
        // register user
    }

    @GetMapping
    public void getUserInfo() {

    }
}
