package nl.tudelft.sem.hour.management.controllers;

import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hour-management")
@Data
public class GreetingController {
    /**
     * Entry point of the microservice, also acts as a sanity check.
     *
     * @return a simple greeting
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody String hello() {
        return "Hello from Hour Management";
    }

    /**
     * Teach people tea is superior to coffee.
     *
     * @return a simple yet important message
     */
    @GetMapping("/coffee")
    @ResponseStatus(HttpStatus.I_AM_A_TEAPOT)
    public @ResponseBody String iamTeapot() {
        return "Congratulations, you get a nice cup of Earl Gray!";
    }
}
