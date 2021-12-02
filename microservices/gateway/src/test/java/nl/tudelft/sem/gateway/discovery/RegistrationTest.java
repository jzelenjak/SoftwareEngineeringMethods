package nl.tudelft.sem.gateway.discovery;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class RegistrationTest {

    @Test
    void testRemoteAddress() {
        Registration registration1 = new Registration("localhost", 8080);
        Registration registration2 = new Registration("test.host", 8081);
        assertEquals("localhost:8080", registration1.remoteAddress());
        assertEquals("test.host:8081", registration2.remoteAddress());
    }

}
