package nl.tudelft.sem.hour.management.validation;

import nl.tudelft.sem.hour.management.config.GatewayConfig;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

public class AsyncAuthValidator extends AsyncBaseValidator {

    /**
     * Constructor.
     *
     * @param gatewayConfig The GatewayConfig.
     */
    public AsyncAuthValidator(GatewayConfig gatewayConfig) {
        super(gatewayConfig);
    }

    @Override
    public Mono<Boolean> validate(HttpHeaders headers, String body) {
        // TODO: Implement authentication checks
        return evaluateNext(headers, body);
    }
}
