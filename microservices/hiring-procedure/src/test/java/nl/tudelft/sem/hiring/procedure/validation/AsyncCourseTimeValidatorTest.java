package nl.tudelft.sem.hiring.procedure.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import nl.tudelft.sem.hiring.procedure.cache.CourseInfoResponseCache;
import nl.tudelft.sem.hiring.procedure.utils.GatewayConfig;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = CourseInfoResponseCache.class)
public class AsyncCourseTimeValidatorTest {

    private static final String AUTHORIZATION_TOKEN = "MyToken";

    @MockBean
    private transient GatewayConfig gatewayConfigMock;

    @Autowired
    private transient CourseInfoResponseCache cache;

    private transient MockWebServer mockWebServer;
    private transient HttpHeaders mockHeaders;

    @BeforeEach
    private void setupEach() throws IOException {
        // Set up the mock server
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        mockHeaders = Mockito.mock(HttpHeaders.class);
        when(mockHeaders.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn(AUTHORIZATION_TOKEN);

        HttpUrl url = mockWebServer.url("/");
        when(gatewayConfigMock.getHost()).thenReturn(url.host());
        when(gatewayConfigMock.getPort()).thenReturn(url.port());

        // Invalidate the course info cache
        cache.invalidateCache();
    }

    @AfterEach
    private void teardownEach() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    public void testConstructor() {
        AsyncCourseTimeValidator validator = new AsyncCourseTimeValidator(cache, 42);
        assertNotNull(validator);
    }

    @Test
    public void testValidate() {
        // Construct validator instance and courseId object
        final long courseId = 1337;
        final AsyncCourseTimeValidator validator = new AsyncCourseTimeValidator(cache, courseId);

        // Fetch the local zoned date time, and make it a valid time
        ZonedDateTime current = ZonedDateTime.now().plusYears(1);

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
        Boolean result = validator.validate(mockHeaders, requestBody)
                .onErrorReturn(false)
                .block();
        assertNotNull(result);
        assertTrue(result);
    }

    @Test
    public void testValidateOverDeadline() {
        // Construct validator instance and courseId object
        final long courseId = 1337;
        final AsyncCourseTimeValidator validator = new AsyncCourseTimeValidator(cache, courseId);

        // Fetch the local zoned date time, and make it a valid time
        ZonedDateTime current = ZonedDateTime.now()
                .plus(2, ChronoUnit.WEEKS);

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
        Boolean result = validator.validate(mockHeaders, requestBody)
                .onErrorReturn(false)
                .block();
        assertNotNull(result);
        assertFalse(result);
    }

    @Test
    public void testValidateNonExistingCourse() {
        // Construct validator instance and courseId object
        long courseId = 1337;
        AsyncCourseTimeValidator validator = new AsyncCourseTimeValidator(cache, courseId);

        // Enqueue a response
        mockWebServer.enqueue(new MockResponse().setResponseCode(HttpStatus.NOT_FOUND.value()));

        // Construct the json object used for testing
        JsonObject json = new JsonObject();
        json.addProperty("courseId", courseId);
        String requestBody = json.toString();

        // Perform the validation
        Boolean result = validator.validate(mockHeaders, requestBody)
                .onErrorReturn(false)
                .block();
        assertNotNull(result);
        assertFalse(result);
    }

}
