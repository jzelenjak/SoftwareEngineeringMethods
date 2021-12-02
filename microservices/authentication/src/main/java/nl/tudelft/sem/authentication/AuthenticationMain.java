package nl.tudelft.sem.authentication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/**
 * The main class for authentication microservice.
 */
@SpringBootApplication
public class AuthenticationMain {

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(AuthenticationMain.class, args);
    }
}
