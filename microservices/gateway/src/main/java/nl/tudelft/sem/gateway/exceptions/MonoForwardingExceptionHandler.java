package nl.tudelft.sem.gateway.exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class MonoForwardingExceptionHandler {

    /**
     * Global exception handler for mono forwarding exceptions.
     * Forwards the response code, and message to the client that performed the initial request.
     *
     * @param exception is the exception that was thrown.
     * @return ResponseEntity that is sent back to the client.
     */
    @ExceptionHandler(MonoForwardingException.class)
    @ResponseBody
    public ResponseEntity<String> handleMonoPropagationException(
            MonoForwardingException exception) {
        return new ResponseEntity<>(exception.getMessage(), exception.getErrorHeaders(),
                exception.getErrorStatus());
    }

}
