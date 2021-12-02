package nl.tudelft.sem.entities.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller class for users.
 */
@RestController
@RequestMapping
public class UserController {
    public UserController() {

    }

    @GetMapping
    public void getUserInfo() {

    }
}
