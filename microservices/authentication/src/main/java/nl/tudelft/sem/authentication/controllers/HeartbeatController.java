package nl.tudelft.sem.authentication.controllers;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.NotAcceptableStatusException;
import org.springframework.web.util.UriComponentsBuilder;


@EnableScheduling
@Getter
@Setter
public class HeartbeatController {

    private static final int duration = 55000;

    // Create web client for executing asynchronous requests
    private WebClient webClient;

    private String destination;

    private Heartbeat heartbeat;


    /**
     * Constructs the GatewayController class.
     */
    public HeartbeatController() {
        this.webClient = WebClient.create();

        // Determine the destination
        destination = UriComponentsBuilder.newInstance()
                .scheme("http").host("localhost").port("8080")
                .path("/discovery/register/auth")
                .build()
                .toUriString();

        // Determine the discovery path
        String discoveryPath = UriComponentsBuilder.newInstance()
                .scheme("http").host("localhost").port("8084")
                .path("/api/auth")
                .build()
                .toUriString();

        heartbeat = new Heartbeat(discoveryPath);
    }

    /**
     * Send a heartbeat to the discovery server every 55 seconds.
     */
    @Scheduled(fixedRate = duration)
    public void sendHeartbeat() {
        webClient
                .post()
                .uri(destination)
                .body(heartbeat, Heartbeat.class)
                .retrieve()
                .onStatus(HttpStatus::isError, clientResponse -> {
                    throw new NotAcceptableStatusException("There has been a problem!");
                });
    }
}

@Data
class Heartbeat {
    private final String path;
}

