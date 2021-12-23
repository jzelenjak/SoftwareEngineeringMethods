package nl.tudelft.sem.hiring.procedure.services;

import com.google.gson.JsonObject;
import lombok.Getter;
import nl.tudelft.sem.hiring.procedure.utils.GatewayConfig;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Getter
@Service
public class NotificationService {
    // WebClient used to communicate with the hiring microservice
    private final transient WebClient webClient;
    private final transient GatewayConfig gatewayConfig;

    /**
     * Constructs a NotificationService instance.
     *
     * @param gatewayConfig is the gateway configuration.
     */
    public NotificationService(GatewayConfig gatewayConfig) {
        this.webClient = WebClient.create();
        this.gatewayConfig = gatewayConfig;
    }

    /**
     * Sends a notification to the authentication microservice for the specified user.
     *
     * @param userId             is the ID of the user to send the notification to.
     * @param message            is the message of the notification.
     * @param authorizationToken is the authorization token of the user.
     */
    public Mono<Void> notify(long userId, String message, String authorizationToken) {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("userId", userId);
        requestBody.addProperty("message", message);

        return webClient.post()
                .uri(UriComponentsBuilder.newInstance()
                        .scheme("http")
                        .host(getGatewayConfig().getHost())
                        .port(getGatewayConfig().getPort())
                        .pathSegment("api", "auth", "notifications", "add")
                        .toUriString())
                .header(HttpHeaders.AUTHORIZATION, authorizationToken)
                .body(Mono.just(requestBody.toString()), String.class)
                .exchange()
                .flatMap(response -> {
                    if (response.statusCode().isError()) {
                        return Mono.error(new ResponseStatusException(response.statusCode(),
                                "Failed to register notification"));
                    }
                    return Mono.empty();
                });
    }
}