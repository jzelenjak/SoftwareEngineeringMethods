package nl.tudelft.sem.heartbeat;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@EnableAutoConfiguration
@SpringBootTest(classes = {HeartbeatConfig.class})
public class HeartbeatTaskTest {

    private static final int PORT = 1337;

    static {
        System.setProperty("server.port", String.valueOf(PORT));
    }

    // Mock web server for testing
    private static MockWebServer mockWebServer;

    @Autowired
    private transient HeartbeatConfig heartbeatConfig;

    @Autowired
    private transient HeartbeatTask heartbeatTask;

    @BeforeAll
    static void setup() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @BeforeEach
    void setupConfig() {
        HttpUrl url = mockWebServer.url("");
        heartbeatConfig.setGatewayHost(url.host());
        heartbeatConfig.setGatewayPort(url.port());
        //heartbeatConfig.setMicroserviceName(MICROSERVICE_NAME);
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void testHeartbeatSchedulingTask() throws Exception {
        // Enqueue
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));

        // Perform task
        heartbeatTask.sendHeartbeat();

        // Verify that task happened
        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        assertEquals("POST", recordedRequest.getMethod());
        assertEquals("/discovery/register/test-microservice", recordedRequest.getPath());

        // Verify body of recorded request
        JsonObject parsed = JsonParser.parseString(recordedRequest
                        .getBody()
                        .readUtf8())
                .getAsJsonObject();
        assertEquals(PORT, parsed.get("port").getAsInt());
    }

    @Test
    void testHeartbeatSchedulingTaskError() throws Exception {
        // Enqueue
        mockWebServer.enqueue(new MockResponse().setResponseCode(400));

        // Perform task
        assertDoesNotThrow(heartbeatTask::sendHeartbeat);

        // Verify that task happened
        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        assertEquals("POST", recordedRequest.getMethod());
        assertEquals("/discovery/register/test-microservice", recordedRequest.getPath());

        // Verify body of recorded request
        JsonObject parsed = JsonParser.parseString(recordedRequest
                        .getBody()
                        .readUtf8())
                .getAsJsonObject();
        assertEquals(PORT, parsed.get("port").getAsInt());
    }

}
