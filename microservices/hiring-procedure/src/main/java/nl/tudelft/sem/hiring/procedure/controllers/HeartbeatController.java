package nl.tudelft.sem.hiring.procedure.controllers;

import lombok.Getter;
import lombok.Setter;
import nl.tudelft.sem.hiring.procedure.dto.Heartbeat;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

@EnableScheduling
@Getter
@Setter
public class HeartbeatController {
    private WebClient webClient;
    private String destination;
    private Heartbeat heartbeat;

    /**
     * Constructor for the HeartbeatController.
     */
    public HeartbeatController() {
        this.webClient = WebClient.create();

        // Determine the destination
        destination = UriComponentsBuilder.newInstance()
            .scheme("http").host("localhost").port("8080")
            .path("/discovery/register/hiring-procedure")
            .build().toUriString();

        // Determine the discovery path
        String discoveryPath = UriComponentsBuilder.newInstance()
            .scheme("http").host("localhost").port("8069")
            .path("/api/hiring-procedure")
            .build().toUriString();

        heartbeat = new Heartbeat(discoveryPath);
    }
}
