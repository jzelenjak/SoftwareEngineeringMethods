package nl.tudelft.sem.users.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * A class that is used to read the gateway host and port from the properties file.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "gateway")
public class GatewayConfig {
    private String host;
    private int port;
}
