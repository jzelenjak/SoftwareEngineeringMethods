package nl.tudelft.sem.courses;

import nl.tudelft.sem.heartbeat.HeartbeatConfig;
import nl.tudelft.sem.jwt.JwtUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({JwtUtils.class, HeartbeatConfig.class})
public class CoursesMain {

    public static void main(String[] args) {
        SpringApplication.run(CoursesMain.class, args);
    }

}
