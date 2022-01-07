package nl.tudelft.sem.hiring.procedure.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import nl.tudelft.sem.hiring.procedure.cache.CourseInfoResponseCache;
import nl.tudelft.sem.hiring.procedure.entities.Submission;
import nl.tudelft.sem.hiring.procedure.entities.SubmissionStatus;
import nl.tudelft.sem.hiring.procedure.services.SubmissionService;
import nl.tudelft.sem.hiring.procedure.utils.GatewayConfig;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class AsyncTaLimitValidatorTest {

    private static final String AUTHORIZATION_TOKEN = "MyToken";

    @MockBean
    private transient GatewayConfig gatewayConfigMock;

    @MockBean
    private transient SubmissionService submissionServiceMock;

    @Autowired
    private transient CourseInfoResponseCache cache;

    private transient MockWebServer mockWebServer;
    private transient HttpHeaders mockHeaders;

    @BeforeEach
    private void setupEach() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        HttpUrl url = mockWebServer.url("/");
        when(gatewayConfigMock.getHost()).thenReturn(url.host());
        when(gatewayConfigMock.getPort()).thenReturn(url.port());

        mockHeaders = Mockito.mock(HttpHeaders.class);
        when(mockHeaders.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn(AUTHORIZATION_TOKEN);

        // Invalidate the cache before each test
        cache.invalidateCache();
    }

    @AfterEach
    private void teardown() throws IOException {
        mockWebServer.shutdown();
    }

    /**
     * Compose the path that is used to fetch course information.
     *
     * @param courseId is the ID of the course.
     * @return Path of the request for fetching course information.
     */
    public static String courseRequestPath(long courseId) {
        return "/api/courses/get/" + courseId;
    }

    @Test
    public void testConstructor() {
        AsyncTaLimitValidator validator = new AsyncTaLimitValidator(submissionServiceMock,
                cache, 1337);
        assertNotNull(validator);
    }

    @Test
    public void testValidatePreciselyEnoughTas() throws InterruptedException {
        // Construct validator instance and courseId object
        final long courseId = 1337;
        final AsyncTaLimitValidator validator = new AsyncTaLimitValidator(submissionServiceMock,
                cache, courseId);

        // Current hiring statistics
        final int currentTaCount = 10;
        final int currentStudentCount = 200;

        // Configure mocks
        LocalDateTime now = LocalDateTime.now();
        Submission submission = new Submission(42, courseId, now);
        submission.setStatus(SubmissionStatus.ACCEPTED);
        List<Submission> submissions =
                new java.util.ArrayList<>(Collections.nCopies(currentTaCount, submission));

        // Add one rejected application to make sure it is not counted
        submission.setStatus(SubmissionStatus.REJECTED);
        submissions.add(submission);
        when(submissionServiceMock.getSubmissionsForCourse(courseId)).thenReturn(submissions);

        // Construct the json object used for testing
        JsonObject json = new JsonObject();
        json.addProperty("numberOfStudents", currentStudentCount);
        String responseBody = json.toString();

        // Enqueue a response
        mockWebServer.enqueue(new MockResponse().setBody(responseBody));

        // Perform the validation
        Mono<Boolean> result = validator.validate(mockHeaders, "");
        assertEquals(Boolean.TRUE, result.block());

        // Check the request by the validator component
        RecordedRequest request = mockWebServer.takeRequest(10, TimeUnit.MILLISECONDS);
        assertNotNull(request);
        assertEquals(HttpMethod.GET.name(), request.getMethod());
        assertEquals(courseRequestPath(courseId), request.getPath());
    }

    @Test
    public void testValidateCeilStudentTaRatio() throws InterruptedException {
        // Construct validator instance and courseId object
        final long courseId = 1337;
        final AsyncTaLimitValidator validator = new AsyncTaLimitValidator(submissionServiceMock,
                cache, courseId);

        // Current hiring statistics
        final int currentTaCount = 10;
        final int currentStudentCount = 181;

        // Configure mocks
        LocalDateTime now = LocalDateTime.now();
        Submission submission = new Submission(42, courseId, now);
        submission.setStatus(SubmissionStatus.ACCEPTED);
        when(submissionServiceMock.getSubmissionsForCourse(courseId)).thenReturn(
                Collections.nCopies(currentTaCount, submission));

        // Construct the json object used for testing
        JsonObject json = new JsonObject();
        json.addProperty("numberOfStudents", currentStudentCount);
        String responseBody = json.toString();

        // Enqueue a response
        mockWebServer.enqueue(new MockResponse().setBody(responseBody));

        // Perform the validation
        Mono<Boolean> result = validator.validate(mockHeaders, "");
        assertEquals(Boolean.TRUE, result.block());

        // Check the request by the validator component
        RecordedRequest request = mockWebServer.takeRequest(10, TimeUnit.MILLISECONDS);
        assertNotNull(request);
        assertEquals(HttpMethod.GET.name(), request.getMethod());
        assertEquals(courseRequestPath(courseId), request.getPath());
    }

    @Test
    public void testValidateTooManyTas() throws InterruptedException {
        // Construct validator instance and courseId object
        final long courseId = 1337;
        final AsyncTaLimitValidator validator = new AsyncTaLimitValidator(submissionServiceMock,
                cache, courseId);

        // Current hiring statistics
        final int currentTaCount = 10;
        final int currentStudentCount = 180;

        // Configure mocks
        LocalDateTime now = LocalDateTime.now();
        Submission submission = new Submission(42, courseId, now);
        submission.setStatus(SubmissionStatus.ACCEPTED);
        when(submissionServiceMock.getSubmissionsForCourse(courseId)).thenReturn(
                Collections.nCopies(currentTaCount, submission));

        // Construct the json object used for testing
        JsonObject json = new JsonObject();
        json.addProperty("numberOfStudents", currentStudentCount);
        String responseBody = json.toString();

        // Enqueue a response
        mockWebServer.enqueue(new MockResponse().setBody(responseBody));

        // Perform the validation
        Mono<Boolean> result = validator.validate(mockHeaders, "");
        assertThrows(ResponseStatusException.class, result::block);

        // Check the request by the validator component
        RecordedRequest request = mockWebServer.takeRequest(10, TimeUnit.MILLISECONDS);
        assertNotNull(request);
        assertEquals(HttpMethod.GET.name(), request.getMethod());
        assertEquals(courseRequestPath(courseId), request.getPath());
    }

    @Test
    public void testValidateCourseDoesNotExist() throws InterruptedException {
        // Construct validator instance and courseId object
        final long courseId = 1337;
        final AsyncTaLimitValidator validator = new AsyncTaLimitValidator(submissionServiceMock,
                cache, courseId);

        // Enqueue a response
        mockWebServer.enqueue(new MockResponse().setResponseCode(HttpStatus.NOT_FOUND.value()));

        // Perform the validation
        Mono<Boolean> result = validator.validate(mockHeaders, "");
        assertThrows(ResponseStatusException.class, result::block);

        // Check the request by the validator component
        RecordedRequest request = mockWebServer.takeRequest(10, TimeUnit.MILLISECONDS);
        assertNotNull(request);
        assertEquals(HttpMethod.GET.name(), request.getMethod());
        assertEquals(courseRequestPath(courseId), request.getPath());
    }

}
