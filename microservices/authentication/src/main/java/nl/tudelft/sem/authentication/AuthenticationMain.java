package nl.tudelft.sem.authentication;

import javax.persistence.ElementCollection;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


/**
 * The main class for authentication microservice.
 */
@SpringBootApplication
@EnableJpaRepositories("nl.tudelft.sem.authentication.repository")
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
