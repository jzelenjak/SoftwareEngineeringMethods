package nl.tudelft.sem.gateway.exceptions;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

public class MonoForwardingException extends Exception {

    // Serialization id
    public static final long serialVersionUID = 1;

    // Status error of the exception
    private final HttpStatus errorStatus;

    // Headers of the exception
    private final HttpHeaders errorHeaders;

    /**
     * Constructs a MonoForwardingException object.
     *
     * @param errorStatus  is the status code of the error.
     * @param errorHeaders is a reference to the headers associated to the exception.
     * @param errorMessage is the error that has occurred.
     */
    public MonoForwardingException(HttpStatus errorStatus, HttpHeaders errorHeaders,
                                   String errorMessage) {
        super(errorMessage);
        this.errorStatus = errorStatus;
        this.errorHeaders = errorHeaders;
    }

    /**
     * Retrieves the error code.
     *
     * @return the error code associated to the forwarded exception.
     */
    public HttpStatus getErrorStatus() {
        return errorStatus;
    }

    /**
     * Retrieves the error headers.
     *
     * @return the error headers associated to the forwarded exception.
     */
    public HttpHeaders getErrorHeaders() {
        return errorHeaders;
    }

}
