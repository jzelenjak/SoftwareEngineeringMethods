package nl.tudelft.sem.hour.management.validation;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import nl.tudelft.sem.hour.management.config.GatewayConfig;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class AsyncHiringValidatorTest {

    private static MockWebServer mockWebServer;

    private static GatewayConfig gatewayConfig;

    @BeforeAll
    static void setup() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        HttpUrl url = mockWebServer.url("");
        gatewayConfig = new GatewayConfig();
        gatewayConfig.setHost(url.host());
        gatewayConfig.setPort(url.port());
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void testConstructor() {
        AsyncHiringValidator validator = new AsyncHiringValidator(gatewayConfig);
        assertNotNull(validator);
    }

    @Test
    void testValidate() {
        // TODO: Implement
    }

}
