package nl.tudelft.sem.hiring.procedure.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import nl.tudelft.sem.hiring.procedure.utils.GatewayConfig;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
public class AsyncCourseTimeValidatorTest {

    @MockBean
    private transient GatewayConfig gatewayConfigMock;

    private static MockWebServer mockWebServer;

    @BeforeAll
    private static void setup() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @BeforeEach
    private void setupEach() {
        HttpUrl url = mockWebServer.url("/");
        when(gatewayConfigMock.getHost()).thenReturn(url.host());
        when(gatewayConfigMock.getPort()).thenReturn(url.port());
    }

    @AfterAll
    private static void teardown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    public void testConstructor() {
        AsyncCourseTimeValidator validator = new AsyncCourseTimeValidator(gatewayConfigMock, 42);
        assertNotNull(validator);
    }

    @Test
    public void testValidate() {
        // Construct validator instance and courseId object
        final long courseId = 1337;
        final AsyncCourseTimeValidator validator = new AsyncCourseTimeValidator(gatewayConfigMock,
                courseId);

        // Fetch the local zoned date time, and make it a valid time
        ZonedDateTime current = ZonedDateTime.now()
                .minus(AsyncCourseTimeValidator.VALID_DURATION)
                .minus(1, ChronoUnit.DAYS);

        // Construct the json object used for testing
        JsonObject json = new JsonObject();
        json.addProperty("startTime", current.toString());
        String responseBody = json.toString();

        // Enqueue a response
        mockWebServer.enqueue(new MockResponse().setBody(responseBody));

        // Construct the json object used for testing
        json = new JsonObject();
        json.addProperty("courseId", courseId);
        String requestBody = json.toString();

        // Perform the validation
        Boolean result = validator.validate(null, requestBody)
                .onErrorReturn(false)
                .block();
        assertNotNull(result);
        assertTrue(result);
    }

    @Test
    public void testValidateOverDeadline() {
        // Construct validator instance and courseId object
        final long courseId = 1337;
        final AsyncCourseTimeValidator validator = new AsyncCourseTimeValidator(gatewayConfigMock,
                courseId);

        // Fetch the local zoned date time, and make it a valid time
        ZonedDateTime current = ZonedDateTime.now()
                .minus(AsyncCourseTimeValidator.VALID_DURATION)
                .plus(1, ChronoUnit.DAYS);

        // Construct the json object used for testing
        JsonObject json = new JsonObject();
        json.addProperty("startTime", current.toString());
        String responseBody = json.toString();

        // Enqueue a response
        mockWebServer.enqueue(new MockResponse().setBody(responseBody));

        // Construct the json object used for testing
        json = new JsonObject();
        json.addProperty("courseId", courseId);
        String requestBody = json.toString();

        // Perform the validation
        Boolean result = validator.validate(null, requestBody)
                .onErrorReturn(false)
                .block();
        assertNotNull(result);
        assertFalse(result);
    }

    @Test
    public void testValidateNonExistingCourse() {
        // Construct validator instance and courseId object
        long courseId = 1337;
        AsyncCourseTimeValidator validator = new AsyncCourseTimeValidator(gatewayConfigMock,
                courseId);

        // Enqueue a response
        mockWebServer.enqueue(new MockResponse().setResponseCode(HttpStatus.NOT_FOUND.value()));

        // Construct the json object used for testing
        JsonObject json = new JsonObject();
        json.addProperty("courseId", courseId);
        String requestBody = json.toString();

        // Perform the validation
        Boolean result = validator.validate(null, requestBody)
                .onErrorReturn(false)
                .block();
        assertNotNull(result);
        assertFalse(result);
    }

}
