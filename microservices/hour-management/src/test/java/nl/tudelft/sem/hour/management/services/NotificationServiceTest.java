package nl.tudelft.sem.hour.management.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.io.IOException;
import nl.tudelft.sem.hour.management.config.GatewayConfig;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@AutoConfigureMockMvc
@SpringBootTest(classes = {GatewayConfig.class, NotificationService.class})
public class NotificationServiceTest {
    private static final long USER_ID = 1L;
    private static final String MESSAGE = "Hello";
    private static final String AUTH_TOKEN = "Bearer VALIDVALID";

    @Autowired
    private transient NotificationService notificationService;

    private static MockWebServer mockWebServer;

    @MockBean
    private transient GatewayConfig gatewayConfig;

    @BeforeAll
    static void setup() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @BeforeEach
    private void setupEach() {
        HttpUrl url = mockWebServer.url("/");
        when(gatewayConfig.getHost()).thenReturn(url.host());
        when(gatewayConfig.getPort()).thenReturn(url.port());
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void testConstructor() {
        NotificationService notificationServiceTest = new NotificationService(gatewayConfig);
        assertThat(notificationServiceTest).isNotNull();
    }

    @Test
    void testNotifySuccessful() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse());

        Mono<Void> result = notificationService.notify(USER_ID, MESSAGE, AUTH_TOKEN);
        assertThat(result.block()).isNull();

        // check that request was made to correct place
        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertEquals("POST", recordedRequest.getMethod());
        assertEquals("/api/auth/notifications/add", recordedRequest.getPath());
    }

    @Test
    void testNotifyFail() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(409));

        Mono<Void> result = notificationService.notify(USER_ID, MESSAGE, AUTH_TOKEN);
        assertThatExceptionOfType(ResponseStatusException.class).isThrownBy(result::block);

        // check that request was made to correct place
        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertEquals("POST", recordedRequest.getMethod());
        assertEquals("/api/auth/notifications/add", recordedRequest.getPath());
    }

}
