package nl.tudelft.sem.hour.management.validation;

import javax.servlet.http.HttpServletRequest;
import nl.tudelft.sem.hour.management.exceptions.AsyncValidationException;
import reactor.core.publisher.Mono;

public interface AsyncValidator {

    /**
     * Sets the next validator in the chain.
     *
     * @param next is the new next validator in the chain.
     */
    void setNext(AsyncValidator next);

    /**
     * Validates the request.
     *
     * @param servletRequest is the request to validate.
     * @return a Mono that emits the validation result.
     */
    Mono<Boolean> validate(HttpServletRequest servletRequest) throws AsyncValidationException;

}
