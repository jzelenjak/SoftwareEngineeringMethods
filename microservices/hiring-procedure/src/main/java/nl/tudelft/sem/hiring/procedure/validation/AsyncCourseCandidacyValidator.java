package nl.tudelft.sem.hiring.procedure.validation;

import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

public class AsyncCourseCandidacyValidator extends AsyncBaseValidator {




    @Override public Mono<Boolean> validate(HttpHeaders headers, String body) {
        // TODO
        return evaluateNext(headers, body);
    }
}
