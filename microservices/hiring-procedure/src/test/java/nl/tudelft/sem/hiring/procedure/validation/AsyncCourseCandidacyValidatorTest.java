package nl.tudelft.sem.hiring.procedure.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import nl.tudelft.sem.hiring.procedure.entities.Application;
import nl.tudelft.sem.hiring.procedure.services.ApplicationService;
import nl.tudelft.sem.hiring.procedure.utils.GatewayConfig;
import nl.tudelft.sem.jwt.JwtUtils;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@AutoConfigureMockMvc
@SpringBootTest(classes = ObjectMapper.class)
public class AsyncCourseCandidacyValidatorTest {

    private static final String AUTHORIZATION_TOKEN = "MyToken";
    private static final String REMOTE_URL = "/api/courses/get-multiple";
    private static final Long USER_ID = 42L;

    private transient MockWebServer mockServer;
    private transient HttpHeaders mockHeaders;

    @MockBean
    private transient JwtUtils jwtUtils;

    @MockBean
    private transient GatewayConfig gatewayConfig;

    @MockBean
    private transient ApplicationService applicationService;

    @Mock
    private transient Jws<Claims> claims;

    @BeforeEach
    private void setupEach() throws IOException {
        mockServer = new MockWebServer();
        mockServer.start();

        mockHeaders = Mockito.mock(HttpHeaders.class);
        when(mockHeaders.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn(AUTHORIZATION_TOKEN);

        HttpUrl url = mockServer.url("/");
        when(gatewayConfig.getHost()).thenReturn(url.host());
        when(gatewayConfig.getPort()).thenReturn(url.port());

        when(jwtUtils.resolveToken(AUTHORIZATION_TOKEN)).thenReturn("");
        when(jwtUtils.validateAndParseClaims(anyString())).thenReturn(claims);
        when(jwtUtils.getUserId(claims)).thenReturn(USER_ID);
    }

    @AfterEach
    private void tearDownEach() throws IOException {
        mockServer.shutdown();
    }

    @Test
    void testConstructor() {
        var validator = new AsyncCourseCandidacyValidator(jwtUtils, applicationService,
                gatewayConfig, 1337);
        assertNotNull(validator);
    }

    @Test
    void testValidateValidCandidacyRequest() throws InterruptedException {
        // Construct objects used for testing
        LocalDateTime now = LocalDateTime.now();
        Application application1 = new Application(1, 42, 1337, now);
        Application application2 = new Application(2, 42, 1338, now);
        Application newApplication = new Application(3, 42, 1339, now);

        // Configure behaviour of the mocks
        when(applicationService.getUnreviewedApplicationsForUser(42)).thenReturn(
                List.of(application1, application2));

        // Enqueue a mock response from the courses microservice
        mockServer.enqueue(new MockResponse().setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(constructCoursesResponseIdenticalDates(application1, application2,
                        newApplication)));

        // Construct the validator
        var validator = new AsyncCourseCandidacyValidator(jwtUtils, applicationService,
                gatewayConfig, 1339);

        // Perform validation action, which should pass
        Mono<Boolean> response = validator.validate(mockHeaders, "");

        // The response should be valid
        assertEquals(Boolean.TRUE, response.block());

        // The request to the course microservice should have been made
        RecordedRequest request = mockServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(request);
        assertEquals(HttpMethod.POST.name(), request.getMethod());
        assertEquals(REMOTE_URL, request.getPath());
        assertEquals(AUTHORIZATION_TOKEN, request.getHeader(HttpHeaders.AUTHORIZATION));
        assertEquals(MediaType.APPLICATION_JSON_VALUE, request.getHeader(HttpHeaders.CONTENT_TYPE));
        assertEquals(constructExpectedCourseRequest(application1, application2, newApplication),
                JsonParser.parseString(request.getBody().readUtf8()));
    }

    @Test
    void testValidateInvalidCandidacyRequestTooManyApplications() throws InterruptedException {
        // Construct objects used for testing
        LocalDateTime now = LocalDateTime.now();
        Application application1 = new Application(2, 42, 1336, now);
        Application application2 = new Application(1, 42, 1337, now);
        Application application3 = new Application(2, 42, 1338, now);
        Application newApplication = new Application(3, 42, 1339, now);

        // Configure behaviour of the mocks
        when(applicationService.getUnreviewedApplicationsForUser(42)).thenReturn(
                List.of(application1, application2, application3));

        // Enqueue a mock response from the courses microservice
        mockServer.enqueue(new MockResponse().setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(constructCoursesResponseIdenticalDates(application1, application2,
                        application3, newApplication)));

        // Construct the validator
        var validator = new AsyncCourseCandidacyValidator(jwtUtils, applicationService,
                gatewayConfig, 1339);

        // Perform validation action, which should fail
        Mono<Boolean> response = validator.validate(mockHeaders, "");

        // The response should be invalid (too many applications for courses in same quarter)
        assertThrows(ResponseStatusException.class, response::block);

        // The request to the course microservice should have been made
        RecordedRequest request = mockServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(request);
        assertEquals(HttpMethod.POST.name(), request.getMethod());
        assertEquals(REMOTE_URL, request.getPath());
        assertEquals(AUTHORIZATION_TOKEN, request.getHeader(HttpHeaders.AUTHORIZATION));
        assertEquals(MediaType.APPLICATION_JSON_VALUE, request.getHeader(HttpHeaders.CONTENT_TYPE));
        assertEquals(constructExpectedCourseRequest(application1, application2, application3,
                        newApplication),
                JsonParser.parseString(request.getBody().readUtf8()));
    }

    @Test
    void testValidateInvalidCandidacyRequestRemoteServerError() throws InterruptedException {
        // Enqueue a mock response from the course microservice
        // Oops, server-side issue
        mockServer.enqueue(new MockResponse().setResponseCode(
                        HttpStatus.INTERNAL_SERVER_ERROR.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // Construct the validator
        var validator = new AsyncCourseCandidacyValidator(jwtUtils, applicationService,
                gatewayConfig, 1339);

        // Perform validation action, which should fail
        Mono<Boolean> response = validator.validate(mockHeaders, "");

        // The response should be invalid (remote server error)
        assertThrows(ResponseStatusException.class, response::block);

        // The request to the course microservice should have been made
        RecordedRequest request = mockServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(request);
        assertEquals(HttpMethod.POST.name(), request.getMethod());
        assertEquals(REMOTE_URL, request.getPath());
        assertEquals(AUTHORIZATION_TOKEN, request.getHeader(HttpHeaders.AUTHORIZATION));
        assertEquals(MediaType.APPLICATION_JSON_VALUE, request.getHeader(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    void testValidateInvalidCandidacyRequestNonExistingCourseNoTaHistory()
            throws InterruptedException {
        // Enqueue a mock response from the course microservice
        mockServer.enqueue(new MockResponse().setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{}"));

        // Construct the validator
        var validator = new AsyncCourseCandidacyValidator(jwtUtils, applicationService,
                gatewayConfig, 1339);

        // Perform validation action, which should fail
        Mono<Boolean> response = validator.validate(mockHeaders, "");

        // The response should be invalid (too many applications for courses in same quarter)
        assertEquals(Boolean.TRUE, response.block());

        // The request to the course microservice should have been made
        RecordedRequest request = mockServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(request);
        assertEquals(HttpMethod.POST.name(), request.getMethod());
        assertEquals(REMOTE_URL, request.getPath());
        assertEquals(AUTHORIZATION_TOKEN, request.getHeader(HttpHeaders.AUTHORIZATION));
        assertEquals(MediaType.APPLICATION_JSON_VALUE, request.getHeader(HttpHeaders.CONTENT_TYPE));
    }

    /**
     * Test helper to generate JSON responses for application with identical start and end
     * dates.
     *
     * @param applications The applications to generate JSON for.
     * @return The JSON response.
     */
    private String constructCoursesResponseIdenticalDates(Application... applications) {
        JsonObject json = new JsonObject();
        for (Application application : applications) {
            JsonObject applicationResponse = new JsonObject();
            applicationResponse.addProperty("courseId", application.getCourseId());

            ZonedDateTime start = ZonedDateTime.now();
            applicationResponse.addProperty("startDate", start.toString());
            applicationResponse.addProperty("endDate", start.plusMonths(3).toString());

            applicationResponse.addProperty("courseCode", RandomStringUtils.random(7));
            applicationResponse.addProperty("numberOfStudents",
                    ThreadLocalRandom.current().nextLong(50, 500));

            // Insert the object in the response
            json.add(String.valueOf(application.getCourseId()), applicationResponse);
        }
        return json.toString();
    }

    /**
     * Test helper to generate JSON requests for the course microservice.
     *
     * @param applications The applications to generate JSON for.
     * @return The expected JSON request.
     */
    private JsonObject constructExpectedCourseRequest(Application... applications) {
        JsonObject json = new JsonObject();
        JsonArray courseIds = new JsonArray();
        Arrays.stream(applications).forEach(a -> courseIds.add(a.getCourseId()));
        json.add("courseIds", courseIds);
        return json;
    }

}
