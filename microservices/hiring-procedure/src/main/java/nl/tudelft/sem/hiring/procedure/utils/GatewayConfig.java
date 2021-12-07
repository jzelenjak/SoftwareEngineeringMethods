package nl.tudelft.sem.hiring.procedure.utils;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class GatewayConfig {
    private String host;
    private int port;

    public GatewayConfig() {
        this.host = "localhost";
        this.port = 8080;
    }
}
