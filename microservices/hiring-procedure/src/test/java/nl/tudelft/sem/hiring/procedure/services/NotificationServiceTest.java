package nl.tudelft.sem.hiring.procedure.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;
import nl.tudelft.sem.hiring.procedure.utils.GatewayConfig;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
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

    private static final String AUTH_TOKEN = "Bearer VALIDVALID";

    private transient long userId;
    private transient String message;

    @Autowired
    private transient NotificationService notificationService;

    private transient MockWebServer mockWebServer;

    @MockBean
    private transient GatewayConfig gatewayConfig;

    @BeforeEach
    void setup() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        // Set user id and message to random values
        userId = ThreadLocalRandom.current().nextLong();
        message = RandomStringUtils.random(20);
    }

    @BeforeEach
    void setupEach() {
        HttpUrl url = mockWebServer.url("/");
        when(gatewayConfig.getHost()).thenReturn(url.host());
        when(gatewayConfig.getPort()).thenReturn(url.port());
    }

    @AfterEach
    void tearDown() throws IOException {
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

        Mono<Void> result = notificationService.notify(userId, message, AUTH_TOKEN);
        assertThat(result.block()).isNull();

        // check that request was made to correct place
        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertEquals("POST", recordedRequest.getMethod());
        assertEquals("/api/auth/notifications/add", recordedRequest.getPath());

        JsonObject recordedBody = JsonParser.parseString(recordedRequest.getBody().readUtf8())
                .getAsJsonObject();
        assertEquals(createExpectedNotificationRequestBody(userId, message), recordedBody);
    }

    @Test
    void testNotifyFail() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse().setResponseCode(409));

        Mono<Void> result = notificationService.notify(userId, message, AUTH_TOKEN);
        assertThatExceptionOfType(ResponseStatusException.class).isThrownBy(result::block);

        // check that request was made to correct place
        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertEquals("POST", recordedRequest.getMethod());
        assertEquals("/api/auth/notifications/add", recordedRequest.getPath());

        JsonObject recordedBody = JsonParser.parseString(recordedRequest.getBody().readUtf8())
                .getAsJsonObject();
        assertEquals(createExpectedNotificationRequestBody(userId, message), recordedBody);
    }

    /**
     * Test helper that creates the expected request body for sending a notification.
     *
     * @param userId  is the ID of the user to send the notification to.
     * @param message is the message to send.
     * @return the expected request body (JSON).
     */
    private JsonObject createExpectedNotificationRequestBody(long userId, String message) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("userId", userId);
        jsonObject.addProperty("message", message);
        return jsonObject;
    }

}
