package nl.tudelft.sem.hiring.procedure.validation;

import nl.tudelft.sem.hiring.procedure.utils.GatewayConfig;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

public class AsyncUserExistsValidator extends AsyncBaseValidator {
    // Gateway configuration
    private final transient GatewayConfig gatewayConfig;

    // Course ID
    private final transient long userId;

    // Web client used to perform requests
    private final transient WebClient webClient;

    /**
     * Constructor of the user exists validator class.
     *
     * @param gatewayConfig is the gateway configuration.
     * @param userId      is the course ID.
     */
    public AsyncUserExistsValidator(GatewayConfig gatewayConfig, long userId) {
        this.gatewayConfig = gatewayConfig;
        this.userId = userId;
        this.webClient = WebClient.create();
    }

    @Override
    public Mono<Boolean> validate(HttpHeaders headers, String body) {
        return webClient.get()
            .uri(UriComponentsBuilder.newInstance().scheme("http")
                .host(gatewayConfig.getHost())
                .port(gatewayConfig.getPort())
                .pathSegment("api", "users", "by-userid")
                .queryParam("userId", userId)
                .toUriString())
            .exchange()
            .flatMap(clientResponse -> {
                if (clientResponse.statusCode().isError()) {
                    return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User not found"));
                }
                // Continue with the request
                return evaluateNext(headers, body);
            });
    }
}
