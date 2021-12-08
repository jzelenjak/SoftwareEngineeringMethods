package nl.tudelft.sem.hour.management.validation;

import javax.validation.constraints.NotNull;
import lombok.Getter;
import nl.tudelft.sem.hour.management.config.GatewayConfig;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

public abstract class AsyncBaseValidator implements AsyncValidator {

    // Contains the next validator in the chain
    @Getter
    private AsyncValidator next;

    // Gateway configuration
    @Getter
    private final GatewayConfig gatewayConfig;

    /**
     * Constructor.
     *
     * @param gatewayConfig The gateway configuration
     */
    public AsyncBaseValidator(@NotNull GatewayConfig gatewayConfig) {
        this.gatewayConfig = gatewayConfig;
    }

    @Override
    public void setNext(AsyncValidator next) {
        this.next = next;
    }

    /**
     * Evaluates the next validator in the chain, if exists.
     *
     * @param headers The headers of the request.
     * @param body The body of the request.
     * @return a Mono containing the result of the validation.
     */
    protected Mono<Boolean> evaluateNext(HttpHeaders headers, String body) {
        if (next == null) {
            return Mono.just(true);
        }
        return next.validate(headers, body);
    }
}
