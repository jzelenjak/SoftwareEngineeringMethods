package nl.tudelft.sem.hiring.procedure.validation;

import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

public interface AsyncValidator {

    /**
     * Utility builder class for building AsyncValidator chains.
     */
    class Builder {
        private AsyncValidator head;
        private AsyncValidator tail;

        /**
         * Private constructor for the builder.
         */
        private Builder() {
        }

        /**
         * Constructs a new builder for the responsibility chain.
         *
         * @return new builder instance.
         */
        public static Builder newBuilder() {
            return new Builder();
        }

        /**
         * Adds a new validator to the chain builder.
         *
         * @param validator is the validator to add.
         * @return the builder instance.
         */
        public Builder addValidator(AsyncValidator validator) {
            if (head == null) {
                head = validator;
            } else {
                tail.setNext(validator);
            }
            tail = validator;
            return this;
        }

        /**
         * Adds a variadic amount of new validators to the chain builder.
         *
         * @param validators are the validators to add.
         * @return the builder instance.
         */
        public Builder addValidators(AsyncValidator... validators) {
            for (AsyncValidator validator : validators) {
                addValidator(validator);
            }
            return this;
        }

        /**
         * Finishes building the chain, returns the head of the chain.
         *
         * @return the head of the chain.
         */
        public AsyncValidator build() {
            return head;
        }
    }

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
     * @param body    body of the HTTP request
     * @return whether request is validated for this part of chain
     */
    Mono<Boolean> validate(HttpHeaders headers, String body);

}
