package nl.tudelft.sem.hiring.procedure;

import nl.tudelft.sem.heartbeat.HeartbeatConfig;
import nl.tudelft.sem.jwt.JwtUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({HeartbeatConfig.class, JwtUtils.class})
public class HiringProcedureMain {

    public static void main(String[] args) {
        SpringApplication.run(HiringProcedureMain.class, args);
    }

}
