package nl.tudelft.sem.gateway.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

public class MonoForwardingExceptionTest {

    @Test
    void testThrow() {
        try {
            throw new MonoForwardingException(HttpStatus.BAD_GATEWAY,
                    HttpHeaders.EMPTY,
                    "Bad Gateway!");
        } catch (MonoForwardingException e) {
            assertEquals(HttpStatus.BAD_GATEWAY, e.getErrorStatus());
            assertEquals(HttpHeaders.EMPTY, e.getErrorHeaders());
            assertEquals(e.getMessage(), "Bad Gateway!");
        }
    }

}
