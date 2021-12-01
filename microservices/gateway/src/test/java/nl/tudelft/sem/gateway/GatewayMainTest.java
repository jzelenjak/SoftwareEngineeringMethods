package nl.tudelft.sem.gateway;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

/**
 * Test class that is SOLELY used to cover the `main` method invocation.
 * Test based on answer suggested by 'davidxxx' in the following thread:
 * https://stackoverflow.com/questions/46650268/how-to-test-main-class-of-spring-boot-application
 */
public class GatewayMainTest {

    @Test
    public void main() {
        assertDoesNotThrow(() -> GatewayMain.main(new String[]{}));
    }

}
