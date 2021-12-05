package nl.tudelft.sem.hour.management.validation;

import javax.servlet.http.HttpServletRequest;
import lombok.Getter;
import nl.tudelft.sem.hour.management.exceptions.AsyncValidationException;
import reactor.core.publisher.Mono;

public abstract class AsyncBaseValidator implements AsyncValidator {

    @Getter
    // Contains the next validator in the chain
    private AsyncValidator next;

    @Override
    public void setNext(AsyncValidator next) {
        this.next = next;
    }

    /**
     * Evaluates the next validator in the chain, if exists.
     *
     * @param servletRequest is the request to validate.
     * @return a Mono containing the result of the validation.
     * @throws AsyncValidationException is thrown if the validation fails.
     */
    public Mono<Boolean> evaluateNext(HttpServletRequest servletRequest)
            throws AsyncValidationException {
        if (next == null) {
            return Mono.just(true);
        }
        return next.validate(servletRequest);
    }
}
