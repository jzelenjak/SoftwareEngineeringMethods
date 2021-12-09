package nl.tudelft.sem.heartbeat;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

@Getter
@Setter
@EnableScheduling
@ComponentScan("nl.tudelft.sem.heartbeat")
@ConfigurationProperties(prefix = "heartbeat")
@PropertySource(value = "classpath:heartbeat_default.properties")
public class HeartbeatConfig {

    private String gatewayHost;

    private int gatewayPort;

    private String microserviceName;

}
