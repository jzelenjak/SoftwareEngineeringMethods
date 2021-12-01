package nl.tudelft.sem.hour.management.controllers;

import lombok.Getter;
import lombok.Setter;
import nl.tudelft.sem.hour.management.dto.Heartbeat;
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
                .path("/discovery/register/hour-management")
                .build().toUriString();

        // Determine the discovery path
        String discoveryPath = UriComponentsBuilder.newInstance()
                .scheme("http").host("localhost").port("8081")
                .path("/api/hour-management")
                .build().toUriString();

        heartbeat = new Heartbeat(discoveryPath);
    }

    /**
     * Send a heartbeat to the discovery server every 55 seconds.
     */
    @Scheduled(fixedRate = 55000)
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
