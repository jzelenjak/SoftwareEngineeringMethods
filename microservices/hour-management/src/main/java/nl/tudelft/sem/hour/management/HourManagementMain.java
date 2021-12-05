package nl.tudelft.sem.hour.management;

import nl.tudelft.sem.heartbeat.HeartbeatConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(HeartbeatConfig.class)
public class HourManagementMain {

    public static void main(String[] args) {
        SpringApplication.run(HourManagementMain.class, args);
    }

}
