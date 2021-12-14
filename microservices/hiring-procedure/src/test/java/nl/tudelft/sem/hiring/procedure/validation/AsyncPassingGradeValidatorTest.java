package nl.tudelft.sem.hiring.procedure.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gson.JsonObject;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import nl.tudelft.sem.hiring.procedure.utils.GatewayConfig;
import nl.tudelft.sem.jwt.JwtUtils;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
public class AsyncPassingGradeValidatorTest {

    // Pattern used for checking the request path
    private static final String EXPECTED_PATH_PATTERN = "/api/courses/grade?courseId=%d&userId=%d";

    @MockBean
    private transient JwtUtils jwtUtilsMock;

    @MockBean
    private transient GatewayConfig gatewayConfigMock;

    @Mock
    private transient HttpHeaders httpHeadersMock;

    @Mock
    private transient Jws<Claims> claimsMock;

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

        // Configure default mock behaviour
        when(httpHeadersMock.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn("");
        when(jwtUtilsMock.resolveToken(any())).thenReturn("");
        when(jwtUtilsMock.validateAndParseClaims(any())).thenReturn(claimsMock);
    }

    @AfterAll
    private static void teardown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    public void testConstructor() {
        AsyncPassingGradeValidator validator = new AsyncPassingGradeValidator(jwtUtilsMock,
                gatewayConfigMock, 1337);
        assertNotNull(validator);
    }

    @Test
    public void testValidatePassingGrade() throws Exception {
        // Construct validator instance and courseId object
        final long userId = 1234;
        final long courseId = 1337;
        final AsyncPassingGradeValidator validator = new AsyncPassingGradeValidator(jwtUtilsMock,
                gatewayConfigMock, courseId);

        // Construct the json object used for testing
        JsonObject json = new JsonObject();
        json.addProperty("grade", AsyncPassingGradeValidator.MIN_PASSING_GRADE + 1.0f);
        String responseBody = json.toString();

        // Configure mock behaviour
        when(jwtUtilsMock.getUserId(claimsMock)).thenReturn(userId);

        // Enqueue a response
        mockWebServer.enqueue(new MockResponse().setBody(responseBody));

        // Perform the validation
        Mono<Boolean> result = validator.validate(httpHeadersMock, "");
        assertEquals(Boolean.TRUE, result.block());

        // Verify mock behaviour
        verify(jwtUtilsMock, times(1)).getUserId(claimsMock);

        // Verify request behaviour
        RecordedRequest recordedRequest = mockWebServer.takeRequest(10, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        assertEquals(HttpMethod.GET.name(), recordedRequest.getMethod());

        String expectedPath = String.format(Locale.ROOT, EXPECTED_PATH_PATTERN, courseId, userId);
        assertEquals(expectedPath, recordedRequest.getPath());
    }

    @Test
    public void testValidateMinimumPassingGrade() throws Exception {
        // Construct validator instance and courseId object
        final long userId = 1234;
        final long courseId = 1337;
        final AsyncPassingGradeValidator validator = new AsyncPassingGradeValidator(jwtUtilsMock,
                gatewayConfigMock, courseId);

        // Construct the json object used for testing
        JsonObject json = new JsonObject();
        json.addProperty("grade", AsyncPassingGradeValidator.MIN_PASSING_GRADE);
        String responseBody = json.toString();

        // Configure mock behaviour
        when(jwtUtilsMock.getUserId(claimsMock)).thenReturn(userId);

        // Enqueue a response
        mockWebServer.enqueue(new MockResponse().setBody(responseBody));

        // Perform the validation
        Mono<Boolean> result = validator.validate(httpHeadersMock, "");
        assertEquals(Boolean.TRUE, result.block());

        // Verify mock behaviour
        verify(jwtUtilsMock, times(1)).getUserId(claimsMock);

        // Verify request behaviour
        RecordedRequest recordedRequest = mockWebServer.takeRequest(10, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        assertEquals(HttpMethod.GET.name(), recordedRequest.getMethod());

        String expectedPath = String.format(Locale.ROOT, EXPECTED_PATH_PATTERN, courseId, userId);
        assertEquals(expectedPath, recordedRequest.getPath());
    }

    @Test
    public void testValidateNotPassed() throws Exception {
        // Construct validator instance and courseId object
        final long userId = 1234;
        final long courseId = 1337;
        final AsyncPassingGradeValidator validator = new AsyncPassingGradeValidator(jwtUtilsMock,
                gatewayConfigMock, courseId);

        // Construct the json object used for testing
        JsonObject json = new JsonObject();
        json.addProperty("grade", AsyncPassingGradeValidator.MIN_PASSING_GRADE - 1.0f);
        String responseBody = json.toString();

        // Configure mock behaviour
        when(jwtUtilsMock.getUserId(claimsMock)).thenReturn(userId);

        // Enqueue a response
        mockWebServer.enqueue(new MockResponse().setBody(responseBody));

        // Perform the validation
        Mono<Boolean> result = validator.validate(httpHeadersMock, "");
        assertThrows(ResponseStatusException.class, result::block);

        // Verify mock behaviour
        verify(jwtUtilsMock, times(1)).getUserId(claimsMock);

        // Verify request behaviour
        RecordedRequest recordedRequest = mockWebServer.takeRequest(10, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        assertEquals(HttpMethod.GET.name(), recordedRequest.getMethod());

        String expectedPath = String.format(Locale.ROOT, EXPECTED_PATH_PATTERN, courseId, userId);
        assertEquals(expectedPath, recordedRequest.getPath());
    }

    @Test
    public void testValidateNoGrade() throws Exception {
        // Construct validator instance and courseId object
        final long userId = 1234;
        final long courseId = 1337;
        final AsyncPassingGradeValidator validator = new AsyncPassingGradeValidator(jwtUtilsMock,
                gatewayConfigMock, courseId);

        // Configure mock behaviour
        when(jwtUtilsMock.getUserId(claimsMock)).thenReturn(userId);

        // Enqueue a response
        mockWebServer.enqueue(new MockResponse().setResponseCode(HttpStatus.NOT_FOUND.value()));

        // Perform the validation
        Mono<Boolean> result = validator.validate(httpHeadersMock, "");
        assertThrows(ResponseStatusException.class, result::block);

        // Verify mock behaviour
        verify(jwtUtilsMock, times(1)).getUserId(claimsMock);

        // Verify request behaviour
        RecordedRequest recordedRequest = mockWebServer.takeRequest(10, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        assertEquals(HttpMethod.GET.name(), recordedRequest.getMethod());

        String expectedPath = String.format(Locale.ROOT, EXPECTED_PATH_PATTERN, courseId, userId);
        assertEquals(expectedPath, recordedRequest.getPath());
    }

}
