package nl.tudelft.sem.hour.management.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class AsyncValidationException extends Exception {

    private static final long serialVersionUID = 1;

    // Status error of the exception
    @Getter
    private final HttpStatus errorStatus;

    public AsyncValidationException(HttpStatus errorStatus, String errorMessage) {
        super(errorMessage);
        this.errorStatus = errorStatus;
    }
}
