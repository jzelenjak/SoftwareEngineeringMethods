package nl.tudelft.sem.authentication;

import nl.tudelft.sem.authentication.jwt.JwtUtils;
import nl.tudelft.sem.heartbeat.HeartbeatConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;


/**
 * The main class for authentication microservice.
 */
@SpringBootApplication
@Import({JwtUtils.class, HeartbeatConfig.class})
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
