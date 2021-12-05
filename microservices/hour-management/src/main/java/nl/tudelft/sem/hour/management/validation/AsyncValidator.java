package nl.tudelft.sem.hour.management.validation;

import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

public interface AsyncValidator {

    /**
     * Sets the next validator in the chain.
     *
     * @param next is the new next validator in the chain.
     */
    void setNext(AsyncValidator next);

    /**
     * Validates a request.
     *
     * @param headers headers of the HTTP request
     * @param body body of the HTTP request
     *
     * @return whether request is validated for this part of chain
     */
    Mono<Boolean> validate(HttpHeaders headers, String body);

}
