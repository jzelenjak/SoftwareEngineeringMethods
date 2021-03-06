package nl.tudelft.sem.hiring.procedure.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.google.gson.JsonObject;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import nl.tudelft.sem.hiring.procedure.cache.CourseInfoResponseCache;
import nl.tudelft.sem.hiring.procedure.entities.Submission;
import nl.tudelft.sem.hiring.procedure.entities.SubmissionStatus;
import nl.tudelft.sem.hiring.procedure.services.SubmissionService;
import nl.tudelft.sem.hiring.procedure.utils.GatewayConfig;
import nl.tudelft.sem.hiring.procedure.validation.AsyncRoleValidator;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@AutoConfigureMockMvc
@SpringBootTest
public class SubmissionControllerTest {
    @Autowired
    private transient MockMvc mockMvc;

    @Autowired
    private transient CourseInfoResponseCache courseInfoResponseCache;

    @MockBean
    private transient SubmissionService submissionService;

    @MockBean
    private transient JwtUtils jwtUtils;

    @Mock
    private transient Jws<Claims> claims;

    @MockBean
    private transient GatewayConfig gatewayConfig;

    private transient MockWebServer mockWebServer;

    private static final long courseId = 2450;
    private static final long userId = 521234;
    private static final long submissionId = 1;
    private static final ZonedDateTime courseStartNextYear = ZonedDateTime.now().plusYears(1);
    private static final String START_TIME = "startDate";
    private static final String BASE_URL = "/";
    private static final String RESOLVED_TOKEN = "yo";
    private static final String COURSE_ID_PARAM = "courseId=";
    private static final String COURSE_ID_STR = "courseId";
    private static final String COURSE_CODE_STR = "courseCode";
    private static final String USER_ID_PARAM = "userId=";
    private static final String SUBMISSION_ID_PARAM = "submissionId";
    private static final String NUMBER_OF_STUDENTS = "numberOfStudents";
    private static final String USER_ID_STR = "userId";
    private static final String START_DATE_STR = "startDate";
    private static final String FINISH_DATE_STR = "endDate";
    private static final String PARAM_STARTER = "?";
    private static final String APPLY_API = "/api/hiring-procedure/apply";
    private static final String HIRE_API = "/api/hiring-procedure/hire-TA";
    private static final String GET_HOURS_API = "/api/hiring-procedure/get-max-hours";
    private static final String GET_CONTRACT_API = "/api/hiring-procedure/get-contract";
    private static final String SET_HOURS_API = "/api/hiring-procedure/set-max-hours";
    private static final String GET_RATING_API = "/api/hiring-procedure/get-rating";
    private static final String SET_RATING_API = "/api/hiring-procedure/rate";
    private static final String GET_SUBMISSIONS_API = "/api/hiring-procedure/get-submissions";
    private static final String GET_STUDENT_API = "/api/hiring-procedure/get-student";
    private static final String GET_METHOD = "GET";
    private static final String NOT_FOUND_ERROR = "Submission not found.";
    private static final String RATING_STRING = "rating";
    private static final String JWT = "eyJhbGciOiJIUzI1NiJ9.eyJSb2xlIjoic3R1ZGVudCIsIklz"
            + "c3VlciI6Iklzc3VlciIsIlVzZXJuYW1lIjoibXRvYWRlciIsImV4cCI6MTYzODYzNDYyMiwiaWF0Ijo"
            + "xNjM4NjM0NjIyfQ.atOFZMwAy3ERmNLmCtrxTGd1eKo1nHeTGAoM9-tXZys";

    private final transient Submission submission
            = new Submission(1, userId, courseId, LocalDateTime.now());

    @BeforeEach
    private void setupEach() throws IOException {
        // Set up the mock server
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        HttpUrl url = mockWebServer.url(BASE_URL);
        when(gatewayConfig.getPort()).thenReturn(url.port());
        when(gatewayConfig.getHost()).thenReturn(url.host());

        // Invalidate the cache before each test
        courseInfoResponseCache.invalidateCache();

        // Object retrieval
        when(submissionService.getSubmission(anyLong())).thenReturn(Optional.of(submission));

        // Default JWT mock behaviour
        when(jwtUtils.resolveToken(JWT)).thenReturn(RESOLVED_TOKEN);
        when(jwtUtils.validateAndParseClaims(RESOLVED_TOKEN)).thenReturn(claims);
        when(jwtUtils.getRole(claims)).thenReturn("LECTURER");
        when(jwtUtils.getUserId(claims)).thenReturn(1L);
    }

    @AfterEach
    private void tearDownEach() throws IOException {
        mockWebServer.shutdown();
    }

    /// Enqueues zero/default-initialized course info object
    private void enqueueZeroInitCourseInfo() {
        ZonedDateTime now = ZonedDateTime.now();
        JsonObject json = new JsonObject();
        json.addProperty(COURSE_ID_STR, 0);
        json.addProperty(COURSE_CODE_STR, "");
        json.addProperty(START_DATE_STR, now.toString());
        json.addProperty("endData", now.toString());
        json.addProperty(NUMBER_OF_STUDENTS, 0);
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(json.toString()));
    }

    @Test
    public void testControllerNoEndpoint() throws Exception {
        mockMvc.perform(get("/api/hiring-procedure/non-existing"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testApplyEndpointPass() throws Exception {
        // Set mocks
        when(jwtUtils.getUserId(claims)).thenReturn(userId);
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.STUDENT.name());
        when(submissionService.checkSameSubmission(userId, courseId)).thenReturn(true);

        // Register listener
        JsonObject json = new JsonObject();
        json.addProperty(START_TIME, courseStartNextYear.toString());
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(json.toString()));
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(generateValidCourseMultiInfoMockResponse(1)));

        // Perform the call
        MvcResult result = mockMvc.perform(post(APPLY_API
                        + PARAM_STARTER + COURSE_ID_PARAM + courseId)
                        .header(HttpHeaders.AUTHORIZATION, JWT))
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk());

        // Extra checks (endpoints called by validator class)
        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        assertEquals(GET_METHOD, recordedRequest.getMethod());
        recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        assertEquals(HttpMethod.POST.name(), recordedRequest.getMethod());

        // Verify that submission was created
        verify(submissionService, times(1)).createSubmission(eq(userId), eq(courseId),
                any(LocalDateTime.class));
    }

    @Test
    public void testApplyEndpointUserNotStudent() throws Exception {
        // Set mocks
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.LECTURER.name());

        // Perform the call
        MvcResult result = mockMvc.perform(post(APPLY_API
                        + PARAM_STARTER + COURSE_ID_PARAM + courseId)
                        .header(HttpHeaders.AUTHORIZATION, JWT))
                .andReturn();
        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testApplyEndpointDeadlinePassed() throws Exception {
        // Set mocks
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.STUDENT.name());

        // Register listener
        JsonObject json = new JsonObject();
        json.addProperty(START_TIME, ZonedDateTime.now()
                .plusWeeks(1).toString());
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(json.toString()));

        // Perform the call
        MvcResult result = mockMvc.perform(post(APPLY_API
                        + PARAM_STARTER + COURSE_ID_PARAM + courseId)
                        .header(HttpHeaders.AUTHORIZATION, JWT))
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isForbidden());

        // Extra checks
        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        assertEquals(GET_METHOD, recordedRequest.getMethod());
    }

    @Test
    public void testApplyEndpointUserApplied() throws Exception {
        // Set mocks
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.STUDENT.name());
        when(jwtUtils.getUserId(claims)).thenReturn(userId);
        when(submissionService.checkSameSubmission(userId, courseId)).thenReturn(false);

        // Register listener
        JsonObject json = new JsonObject();
        json.addProperty(START_TIME, courseStartNextYear.toString());
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(json.toString()));
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(generateValidCourseMultiInfoMockResponse(1)));

        // Perform the call
        MvcResult result = mockMvc.perform(post(APPLY_API
                        + PARAM_STARTER + COURSE_ID_PARAM + courseId)
                        .header(HttpHeaders.AUTHORIZATION, JWT))
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isForbidden());

        // Extra checks (endpoints called by validator class)
        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        assertEquals(GET_METHOD, recordedRequest.getMethod());
        recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        assertEquals(HttpMethod.POST.name(), recordedRequest.getMethod());
    }

    @Test
    public void testApplyEndpointCourseNotFound() throws Exception {
        // Set mocks
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.STUDENT.name());

        // Register listener
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404));

        // Perform the call
        MvcResult result = mockMvc.perform(post(APPLY_API
                        + PARAM_STARTER + COURSE_ID_PARAM + courseId)
                        .header(HttpHeaders.AUTHORIZATION, JWT))
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isNotFound());

        // Extra checks
        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        assertEquals(GET_METHOD, recordedRequest.getMethod());
    }

    @Test
    public void testApplyEndpointInvalidToken() throws Exception {
        // Set mocks
        when(jwtUtils.resolveToken(JWT)).thenReturn(RESOLVED_TOKEN);
        when(jwtUtils.validateAndParseClaims(RESOLVED_TOKEN)).thenReturn(null);

        // Perform the call
        MvcResult result = mockMvc.perform(post(APPLY_API
                        + PARAM_STARTER + COURSE_ID_PARAM + courseId)
                        .header(HttpHeaders.AUTHORIZATION, JWT))
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testGetAllEndpointPass() throws Exception {
        // Set mocks
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.LECTURER.name());

        // Perform the call
        MvcResult result = mockMvc.perform(get("/api/hiring-procedure/get-all-submissions")
                        .header(HttpHeaders.AUTHORIZATION, JWT))
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetAllEndpointNotLecturer() throws Exception {
        // Set mocks
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.STUDENT.name());

        // Perform the call
        MvcResult result = mockMvc.perform(get("/api/hiring-procedure/get-all-submissions")
                        .header(HttpHeaders.AUTHORIZATION, JWT))
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testGetAllEndpointInvalidToken() throws Exception {
        // Set mocks
        when(jwtUtils.resolveToken(JWT)).thenReturn(RESOLVED_TOKEN);
        when(jwtUtils.validateAndParseClaims(RESOLVED_TOKEN)).thenReturn(null);

        // Perform the call
        MvcResult result = mockMvc.perform(get("/api/hiring-procedure/get-all-submissions")
                        .header(HttpHeaders.AUTHORIZATION, JWT))
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testGetSubmissionsEndpointPass() throws Exception {
        // Set mocks
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.LECTURER.name());

        // Enqueue mock responses
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200));

        // Perform the call
        MvcResult result = mockMvc.perform(get(GET_SUBMISSIONS_API + PARAM_STARTER
                        + COURSE_ID_PARAM + courseId)
                        .header(HttpHeaders.AUTHORIZATION, JWT))
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetSubmissionsEndpointNotLecturer() throws Exception {
        // Set mocks
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.STUDENT.name());

        // Perform the call
        MvcResult result = mockMvc.perform(get(GET_SUBMISSIONS_API + PARAM_STARTER
                        + COURSE_ID_PARAM + courseId)
                        .header(HttpHeaders.AUTHORIZATION, JWT))
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testGetSubmissionsEndpointInvalidToken() throws Exception {
        // Set mocks
        when(jwtUtils.resolveToken(JWT)).thenReturn(RESOLVED_TOKEN);
        when(jwtUtils.validateAndParseClaims(RESOLVED_TOKEN)).thenReturn(null);

        // Perform the call
        MvcResult result = mockMvc.perform(get(GET_SUBMISSIONS_API + PARAM_STARTER
                        + COURSE_ID_PARAM + courseId)
                        .header(HttpHeaders.AUTHORIZATION, JWT))
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testHireEndpointPass() throws Exception {
        // Set mocks
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.LECTURER.name());
        when(submissionService.checkCandidate(submissionId)).thenReturn(true);
        when(submissionService.getSubmission(submissionId)).thenReturn(Optional.of(submission));

        // Register listener
        JsonObject json = new JsonObject();
        json.addProperty(START_TIME, courseStartNextYear.toString());
        json.addProperty(NUMBER_OF_STUDENTS, 0);
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200));
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(json.toString()));
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200));
        enqueueZeroInitCourseInfo();

        // Perform the call
        MvcResult result = mockMvc.perform(post(HIRE_API)
                        .queryParam(SUBMISSION_ID_PARAM, String.valueOf(submissionId))
                        .header(HttpHeaders.AUTHORIZATION, JWT))
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk());

        // Extra checks
        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        assertEquals(GET_METHOD, recordedRequest.getMethod());
        recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        assertEquals(GET_METHOD, recordedRequest.getMethod());
        recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        assertEquals(GET_METHOD, recordedRequest.getMethod());
    }

    @Test
    public void testHireEndpointSubmissionNotFoundFailed() throws Exception {
        // Set mocks
        when(jwtUtils.resolveToken(JWT)).thenReturn(RESOLVED_TOKEN);
        when(jwtUtils.validateAndParseClaims(RESOLVED_TOKEN)).thenReturn(claims);
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.LECTURER.name());
        when(submissionService.getSubmission(submissionId)).thenReturn(Optional.empty());

        // Perform the call
        MvcResult result = mockMvc.perform(post(HIRE_API)
                        .queryParam(SUBMISSION_ID_PARAM, String.valueOf(submissionId))
                        .header(HttpHeaders.AUTHORIZATION, JWT))
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testHireEndpointInvalidToken() throws Exception {
        // Set mocks
        when(jwtUtils.resolveToken(JWT)).thenReturn(RESOLVED_TOKEN);
        when(jwtUtils.validateAndParseClaims(RESOLVED_TOKEN)).thenReturn(null);

        // Perform the call
        MvcResult result = mockMvc.perform(post(HIRE_API)
                        .queryParam(SUBMISSION_ID_PARAM, String.valueOf(submissionId))
                        .header(HttpHeaders.AUTHORIZATION, JWT))
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testHireEndpointNotLecturer() throws Exception {
        // Set mocks
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.STUDENT.name());

        // Perform the call
        MvcResult result = mockMvc.perform(post(HIRE_API)
                        .queryParam(SUBMISSION_ID_PARAM, String.valueOf(submissionId))
                        .header(HttpHeaders.AUTHORIZATION, JWT))
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testHireEndpointCourseNotFound() throws Exception {
        // Set mocks
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.LECTURER.name());

        // Register listener
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200));
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody("Course not found."));

        // Perform the call
        MvcResult result = mockMvc.perform(post(HIRE_API)
                        .queryParam(SUBMISSION_ID_PARAM, String.valueOf(submissionId))
                        .header(HttpHeaders.AUTHORIZATION, JWT))
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isNotFound());

        // Extra checks
        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        assertEquals(GET_METHOD, recordedRequest.getMethod());
    }

    @Test
    public void testHireEndpointUserNotFound() throws Exception {
        // Set mocks
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.LECTURER.name());

        // Register listener
        JsonObject json = new JsonObject();
        json.addProperty(START_TIME, courseStartNextYear.toString());
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200));
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(json.toString()));
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404));

        // Perform the call
        MvcResult result = mockMvc.perform(post(HIRE_API)
                        .queryParam(SUBMISSION_ID_PARAM,
                                String.valueOf(submission.getSubmissionId()))
                        .header(HttpHeaders.AUTHORIZATION, JWT))
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isNotFound());

        // Extra checks
        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        assertEquals(GET_METHOD, recordedRequest.getMethod());
        recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        assertEquals(GET_METHOD, recordedRequest.getMethod());
        recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        assertEquals(GET_METHOD, recordedRequest.getMethod());
    }

    @Test
    public void testHireEndpointNotViable() throws Exception {
        // Set mocks
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.LECTURER.name());
        when(submissionService.checkCandidate(submissionId)).thenReturn(false);

        // Register listener
        JsonObject json = new JsonObject();
        json.addProperty(START_TIME, courseStartNextYear.toString());
        json.addProperty(NUMBER_OF_STUDENTS, 0);
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200));
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(json.toString()));
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200));

        // Perform the call
        MvcResult result = mockMvc.perform(post(HIRE_API)
                        .queryParam(SUBMISSION_ID_PARAM, String.valueOf(submissionId))
                        .header(HttpHeaders.AUTHORIZATION, JWT))
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isForbidden());

        // Extra checks
        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        assertEquals(GET_METHOD, recordedRequest.getMethod());
        recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        assertEquals(GET_METHOD, recordedRequest.getMethod());
    }

    @Test
    void testWithdraw() throws Exception {
        // Create new submission
        ZonedDateTime start = ZonedDateTime.now();
        Submission submission = new Submission(userId, courseId, start.toLocalDateTime());
        when(submissionService.getSubmission(userId, courseId)).thenReturn(Optional.of(submission));

        // Configure request mock
        when(jwtUtils.getUserId(Mockito.any())).thenReturn(userId);
        when(jwtUtils.resolveToken(Mockito.any())).thenReturn("");
        when(jwtUtils.validateAndParseClaims(Mockito.any())).thenReturn(claims);
        when(jwtUtils.getRole(Mockito.any())).thenReturn(AsyncRoleValidator.Roles.STUDENT.name());

        // Enqueue course validator response
        JsonObject json = new JsonObject();
        json.addProperty(START_TIME, courseStartNextYear.toString());
        mockWebServer.enqueue(new MockResponse().setBody(json.toString()));

        // Create request body and perform the call
        MvcResult result = mockMvc.perform(post("/api/hiring-procedure/withdraw")
                        .header(HttpHeaders.AUTHORIZATION, "")
                        .queryParam(COURSE_ID_STR, String.valueOf(courseId)))
                .andReturn();

        // Await the call
        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk());

        // Verify that there was an attempt to change the submission status
        verify(submissionService, times(1)).withdrawSubmission(submission.getSubmissionId());
    }

    @Test
    void testWithdrawNonExisting() throws Exception {
        // Create new submission mock behaviour
        when(submissionService.getSubmission(userId, courseId))
            .thenReturn(Optional.empty());

        // Configure request mock
        when(jwtUtils.getUserId(Mockito.any())).thenReturn(userId);
        when(jwtUtils.resolveToken(Mockito.any())).thenReturn("");
        when(jwtUtils.validateAndParseClaims(Mockito.any())).thenReturn(claims);
        when(jwtUtils.getRole(Mockito.any())).thenReturn(AsyncRoleValidator.Roles.STUDENT.name());

        // Enqueue course validator response
        JsonObject json = new JsonObject();
        json.addProperty(START_TIME, courseStartNextYear.toString());
        mockWebServer.enqueue(new MockResponse().setBody(json.toString()));

        // Create request body and perform the call
        MvcResult result = mockMvc.perform(post("/api/hiring-procedure/withdraw")
                        .header(HttpHeaders.AUTHORIZATION, "")
                        .queryParam(COURSE_ID_STR, String.valueOf(courseId)))
                .andReturn();

        // Await the call
        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isNotFound());

        // Verify that there was no attempt to change the submission status
        verify(submissionService, times(0)).withdrawSubmission(anyLong());
    }

    @Test
    void testWithdrawAlreadyProcessed() throws Exception {
        // Create new submission
        ZonedDateTime start = ZonedDateTime.now();
        Submission submission = new Submission(userId, courseId, start.toLocalDateTime());
        submission.setStatus(SubmissionStatus.ACCEPTED);
        when(submissionService.getSubmission(userId, courseId)).thenReturn(Optional.of(submission));

        // Configure request mock
        when(jwtUtils.getUserId(Mockito.any())).thenReturn(userId);
        when(jwtUtils.resolveToken(Mockito.any())).thenReturn("");
        when(jwtUtils.validateAndParseClaims(Mockito.any())).thenReturn(claims);
        when(jwtUtils.getRole(Mockito.any())).thenReturn(AsyncRoleValidator.Roles.STUDENT.name());

        // Enqueue course validator response
        JsonObject json = new JsonObject();
        json.addProperty(START_TIME, courseStartNextYear.toString());
        mockWebServer.enqueue(new MockResponse().setBody(json.toString()));

        // Create request body and perform the call
        MvcResult result = mockMvc.perform(post("/api/hiring-procedure/withdraw")
                        .header(HttpHeaders.AUTHORIZATION, "")
                        .queryParam(COURSE_ID_STR, String.valueOf(courseId)))
                .andReturn();

        // Await the call
        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isMethodNotAllowed());

        // Verify that there was no attempt to change the submission status
        verify(submissionService, times(0)).withdrawSubmission(submission.getSubmissionId());
    }

    @Test
    void testReject() throws Exception {
        // Submission info
        long submissionId = 1337L;
        Submission submissionMock = Mockito.mock(Submission.class);
        when(submissionService.getSubmission(submissionId)).thenReturn(Optional.of(submissionMock));
        when(submissionService.checkCandidate(submissionId)).thenReturn(true);

        // Configure request mock
        when(jwtUtils.getUserId(Mockito.any())).thenReturn(userId);
        when(jwtUtils.resolveToken(Mockito.any())).thenReturn("");
        when(jwtUtils.validateAndParseClaims(Mockito.any())).thenReturn(claims);
        when(jwtUtils.getRole(Mockito.any())).thenReturn(AsyncRoleValidator.Roles.LECTURER.name());

        // Enqueue mock responses
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200));

        // Create request body and perform the call
        MvcResult result = mockMvc.perform(post("/api/hiring-procedure/reject")
                        .header(HttpHeaders.AUTHORIZATION, "")
                        .queryParam(SUBMISSION_ID_PARAM, String.valueOf(submissionId)))
                .andReturn();

        // Await the call
        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk());

        // Verify that there was an attempt to change the submission status
        verify(submissionService, times(1)).getSubmission(submissionId);
        verify(submissionService, times(1)).rejectSubmission(submissionId);
    }


    @Test
    void testRejectNonExisting() throws Exception {
        // Submission info
        long submissionId = 1337L;
        when(submissionService.getSubmission(submissionId)).thenReturn(Optional.empty());

        // Configure request mock
        when(jwtUtils.getUserId(Mockito.any())).thenReturn(userId);
        when(jwtUtils.resolveToken(Mockito.any())).thenReturn("");
        when(jwtUtils.validateAndParseClaims(Mockito.any())).thenReturn(claims);
        when(jwtUtils.getRole(Mockito.any())).thenReturn(AsyncRoleValidator.Roles.LECTURER.name());

        // Create request body and perform the call
        MvcResult result = mockMvc.perform(post("/api/hiring-procedure/reject")
                        .header(HttpHeaders.AUTHORIZATION, "")
                        .queryParam(SUBMISSION_ID_PARAM, String.valueOf(submissionId)))
                .andReturn();

        // Await the call
        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isNotFound());

        // Verify that there was an attempt to fetch the submission
        verify(submissionService, times(1)).getSubmission(submissionId);
        verify(submissionService, times(0)).rejectSubmission(submissionId);
    }

    @Test
    void testRejectAlreadyProcessed() throws Exception {
        // Submission info
        long submissionId = 1337L;
        Submission submissionMock = Mockito.mock(Submission.class);
        when(submissionService.getSubmission(submissionId))
                .thenReturn(Optional.of(submissionMock));
        when(submissionMock.getStatus()).thenReturn(SubmissionStatus.ACCEPTED);

        // Configure request mock
        when(jwtUtils.getUserId(Mockito.any())).thenReturn(userId);
        when(jwtUtils.resolveToken(Mockito.any())).thenReturn("");
        when(jwtUtils.validateAndParseClaims(Mockito.any())).thenReturn(claims);
        when(jwtUtils.getRole(Mockito.any())).thenReturn(AsyncRoleValidator.Roles.LECTURER.name());

        // Enqueue mock responses
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200));

        // Create request body and perform the call
        MvcResult result = mockMvc.perform(post("/api/hiring-procedure/reject")
                        .header(HttpHeaders.AUTHORIZATION, "")
                        .queryParam(SUBMISSION_ID_PARAM, String.valueOf(submissionId)))
                .andReturn();

        // Await the call
        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isMethodNotAllowed());

        // Verify that there was an attempt to change the submission status
        verify(submissionService, times(1)).getSubmission(submissionId);
        verify(submissionService, times(0)).rejectSubmission(submissionId);
    }

    @Test
    void testGetOwnContractSuccess() throws Exception {
        when(jwtUtils.resolveToken(JWT)).thenReturn(RESOLVED_TOKEN);
        when(jwtUtils.validateAndParseClaims(RESOLVED_TOKEN)).thenReturn(claims);
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.STUDENT.name());

        JsonObject json = new JsonObject();
        json.addProperty("id", courseId);
        json.addProperty(COURSE_CODE_STR, "CSE1215");
        json.addProperty(START_DATE_STR, courseStartNextYear.toString());
        json.addProperty(FINISH_DATE_STR, courseStartNextYear.plusMonths(3).toString());
        json.addProperty("numStudents", 420);
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(json.toString()));

        // Perform the call
        MvcResult result = mockMvc.perform(get(GET_CONTRACT_API)
                        .queryParam("name", "JegorSus")
                        .queryParam(COURSE_ID_STR, String.valueOf(courseId))
                        .header(HttpHeaders.AUTHORIZATION, JWT))
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk());
    }

    @Test
    void testGetContractSuccess() throws Exception {
        when(jwtUtils.resolveToken(JWT)).thenReturn(RESOLVED_TOKEN);
        when(jwtUtils.validateAndParseClaims(RESOLVED_TOKEN)).thenReturn(claims);
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.LECTURER.name());

        JsonObject json1 = new JsonObject();
        json1.addProperty(USER_ID_STR, userId);
        json1.addProperty("username", "aimpostor");
        json1.addProperty("firstName", "Sussy");
        json1.addProperty("lastName", "Baka");
        json1.addProperty("role", "STUDENT");
        JsonObject json2 = new JsonObject();
        json2.addProperty("id", courseId);
        json2.addProperty(COURSE_CODE_STR, "CSE1215");
        json2.addProperty(START_DATE_STR, courseStartNextYear.toString());
        json2.addProperty(FINISH_DATE_STR, courseStartNextYear.plusMonths(3).toString());
        json2.addProperty("numStudents", 420);
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(json1.toString()));
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(json2.toString()));

        // Perform the call
        MvcResult result = mockMvc.perform(get(GET_CONTRACT_API)
                .queryParam(USER_ID_STR, String.valueOf(userId))
                .queryParam(COURSE_ID_STR, String.valueOf(courseId))
                .header(HttpHeaders.AUTHORIZATION, JWT))
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk());
    }

    @Test
    void testGetContractNoCourse() throws Exception {
        when(jwtUtils.resolveToken(JWT)).thenReturn(RESOLVED_TOKEN);
        when(jwtUtils.validateAndParseClaims(RESOLVED_TOKEN)).thenReturn(claims);
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.LECTURER.name());

        JsonObject json1 = new JsonObject();
        json1.addProperty(USER_ID_STR, userId);
        json1.addProperty("username", "aimpostor");
        json1.addProperty("firstName", "Sussy");
        json1.addProperty("lastName", "Baka");
        json1.addProperty("role", "STUDENT");
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(json1.toString()));
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404));

        // Perform the call
        MvcResult result = mockMvc.perform(get(GET_CONTRACT_API)
                .queryParam(USER_ID_STR, String.valueOf(userId))
                .queryParam(COURSE_ID_STR, String.valueOf(courseId))
                .header(HttpHeaders.AUTHORIZATION, JWT))
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetContractNoUser() throws Exception {
        when(jwtUtils.resolveToken(JWT)).thenReturn(RESOLVED_TOKEN);
        when(jwtUtils.validateAndParseClaims(RESOLVED_TOKEN)).thenReturn(claims);
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.LECTURER.name());


        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404));

        // Perform the call
        MvcResult result = mockMvc.perform(get(GET_CONTRACT_API)
                .queryParam(USER_ID_STR, String.valueOf(userId))
                .queryParam(COURSE_ID_STR, String.valueOf(courseId))
                .header(HttpHeaders.AUTHORIZATION, JWT))
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetMaxHoursLecturerPass() throws Exception {
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.LECTURER.name());

        // Perform the call
        MvcResult result = mockMvc.perform(get(GET_HOURS_API)
                        .queryParam(USER_ID_STR, String.valueOf(userId))
                        .queryParam(COURSE_ID_STR, String.valueOf(courseId))
                        .header(HttpHeaders.AUTHORIZATION, JWT))
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk());
    }

    @Test
    void testGetMaxHoursTaPass() throws Exception {
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.STUDENT.name());

        // Perform the call
        MvcResult result = mockMvc.perform(get(GET_HOURS_API)
                        .queryParam(COURSE_ID_STR, String.valueOf(courseId))
                        .header(HttpHeaders.AUTHORIZATION, JWT))
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk());
    }

    @Test
    void testGetMaxHoursLecturerNoSubmission() throws Exception {
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.LECTURER.name());
        when(submissionService.getMaxHours(userId, courseId))
                .thenThrow(new NoSuchElementException(NOT_FOUND_ERROR));

        // Perform the call
        MvcResult result = mockMvc.perform(get(GET_HOURS_API)
                        .queryParam(USER_ID_STR, String.valueOf(userId))
                        .queryParam(COURSE_ID_STR, String.valueOf(courseId))
                        .header(HttpHeaders.AUTHORIZATION, JWT))
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetMaxHoursTaNoSubmission() throws Exception {
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.STUDENT.name());
        when(submissionService.getMaxHours(anyLong(), eq(courseId)))
                .thenThrow(new NoSuchElementException(NOT_FOUND_ERROR));

        // Perform the call
        MvcResult result = mockMvc.perform(get(GET_HOURS_API)
                        .queryParam(COURSE_ID_STR, String.valueOf(courseId))
                        .header(HttpHeaders.AUTHORIZATION, JWT))
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetMaxHoursLecturerNotPermitted() throws Exception {
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.LECTURER.name());

        // Perform the call
        MvcResult result = mockMvc.perform(get(GET_HOURS_API)
                        .queryParam(COURSE_ID_STR, String.valueOf(courseId))
                        .header(HttpHeaders.AUTHORIZATION, JWT))
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetMaxHoursTaNotPermitted() throws Exception {
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.STUDENT.name());

        // Perform the call
        MvcResult result = mockMvc.perform(get(GET_HOURS_API)
                        .queryParam(USER_ID_STR, String.valueOf(userId))
                        .queryParam(COURSE_ID_STR, String.valueOf(courseId))
                        .header(HttpHeaders.AUTHORIZATION, JWT))
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isForbidden());
    }

    @Test
    void testSetMaxHoursPass() throws Exception {
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.LECTURER.name());

        JsonObject json = new JsonObject();
        json.addProperty("maxHours", 50);

        // Perform the call
        MvcResult result = mockMvc.perform(post(SET_HOURS_API)
                        .queryParam(SUBMISSION_ID_PARAM, String.valueOf(1))
                        .content(json.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, JWT))
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk());
    }

    @Test
    void testSetMaxHoursBadBody() throws Exception {
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.LECTURER.name());

        JsonObject json = new JsonObject();
        json.addProperty("max_hours", 50);

        // Perform the call
        MvcResult result = mockMvc.perform(post(SET_HOURS_API)
                        .queryParam(SUBMISSION_ID_PARAM, String.valueOf(1))
                        .content(json.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, JWT))
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSetMaxHoursStudent() throws Exception {
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.STUDENT.name());

        JsonObject json = new JsonObject();
        json.addProperty("maxHours", 50);

        // Perform the call
        MvcResult result = mockMvc.perform(post(SET_HOURS_API)
                        .queryParam(SUBMISSION_ID_PARAM, String.valueOf(1))
                        .content(json.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, JWT))
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isForbidden());
    }

    @Test
    void testSetMaxHoursNoSubmission() throws Exception {
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.LECTURER.name());
        doThrow(new NoSuchElementException(NOT_FOUND_ERROR))
                .when(submissionService).setMaxHours(1, 50);

        JsonObject json = new JsonObject();
        json.addProperty("maxHours", 50);

        // Perform the call
        MvcResult result = mockMvc.perform(post(SET_HOURS_API)
                        .queryParam(SUBMISSION_ID_PARAM, String.valueOf(1))
                        .content(json.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, JWT))
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetRatingLecturerPass() throws Exception {
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.LECTURER.name());

        // Perform the call
        MvcResult result = mockMvc.perform(get(GET_RATING_API)
                        .queryParam(USER_ID_STR, String.valueOf(userId))
                        .queryParam(COURSE_ID_STR, String.valueOf(courseId))
                        .header(HttpHeaders.AUTHORIZATION, JWT))
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk());
    }

    @Test
    void testGetRatingTaPass() throws Exception {
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.STUDENT.name());

        // Perform the call
        MvcResult result = mockMvc.perform(get(GET_RATING_API)
                        .queryParam(COURSE_ID_STR, String.valueOf(courseId))
                        .header(HttpHeaders.AUTHORIZATION, JWT))
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk());
    }

    @Test
    void testGetRatingTaForbidden() throws Exception {
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.STUDENT.name());

        // Perform the call
        MvcResult result = mockMvc.perform(get(GET_RATING_API)
                        .queryParam(USER_ID_STR, String.valueOf(userId))
                        .queryParam(COURSE_ID_STR, String.valueOf(courseId))
                        .header(HttpHeaders.AUTHORIZATION, JWT))
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetRatingLecturerForbidden() throws Exception {
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.LECTURER.name());

        // Perform the call
        MvcResult result = mockMvc.perform(get(GET_RATING_API)
                        .queryParam(COURSE_ID_STR, String.valueOf(courseId))
                        .header(HttpHeaders.AUTHORIZATION, JWT))
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetRatingNoSubmission() throws Exception {
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.LECTURER.name());
        doThrow(new NoSuchElementException("No submission was found."))
                .when(submissionService).getRating(userId, courseId);

        // Perform the call
        MvcResult result = mockMvc.perform(get(GET_RATING_API)
                        .queryParam(USER_ID_STR, String.valueOf(userId))
                        .queryParam(COURSE_ID_STR, String.valueOf(courseId))
                        .header(HttpHeaders.AUTHORIZATION, JWT))
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetRatingNotApproved() throws Exception {
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.LECTURER.name());
        doThrow(new IllegalStateException("Submission is not approved."))
                .when(submissionService).getRating(userId, courseId);

        // Perform the call
        MvcResult result = mockMvc.perform(get(GET_RATING_API)
                        .queryParam(USER_ID_STR, String.valueOf(userId))
                        .queryParam(COURSE_ID_STR, String.valueOf(courseId))
                        .header(HttpHeaders.AUTHORIZATION, JWT))
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isForbidden())
                .andExpect(status().reason("The respective submission has not been approved"));
    }

    @Test
    void testGetRatingNotRated() throws Exception {
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.LECTURER.name());
        doThrow(new IllegalStateException("Submission is not yet rated."))
                .when(submissionService).getRating(userId, courseId);

        // Perform the call
        MvcResult result = mockMvc.perform(get(GET_RATING_API)
                        .queryParam(USER_ID_STR, String.valueOf(userId))
                        .queryParam(COURSE_ID_STR, String.valueOf(courseId))
                        .header(HttpHeaders.AUTHORIZATION, JWT))
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isForbidden())
                .andExpect(status().reason("The respective student has no rating for this course"));
    }

    @Test
    void testSetRatingPass() throws Exception {
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.LECTURER.name());

        JsonObject json = new JsonObject();
        json.addProperty(RATING_STRING, 7.5);

        // Perform the call
        MvcResult result = mockMvc.perform(post(SET_RATING_API)
                        .queryParam(SUBMISSION_ID_PARAM, String.valueOf(1))
                        .content(json.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, JWT))
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk());
    }

    @Test
    void testSetRatingBadBody() throws Exception {
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.LECTURER.name());

        JsonObject json = new JsonObject();
        json.addProperty("ratingValue", 7.5);

        // Perform the call
        MvcResult result = mockMvc.perform(post(SET_RATING_API)
                        .queryParam(SUBMISSION_ID_PARAM, String.valueOf(1))
                        .content(json.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, JWT))
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isBadRequest())
                .andExpect(status()
                        .reason("Body was not configured accordingly. Please see documentation"));
    }

    @Test
    void testSetRatingStudent() throws Exception {
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.STUDENT.name());

        JsonObject json = new JsonObject();
        json.addProperty(RATING_STRING, 7.5);

        // Perform the call
        MvcResult result = mockMvc.perform(post(SET_RATING_API)
                        .queryParam(SUBMISSION_ID_PARAM, String.valueOf(1))
                        .content(json.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, JWT))
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isForbidden());
    }

    @Test
    void testSetRatingInvalidRating() throws Exception {
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.LECTURER.name());

        JsonObject json = new JsonObject();
        json.addProperty(RATING_STRING, 11);

        // Perform the call
        MvcResult result = mockMvc.perform(post(SET_RATING_API)
                        .queryParam(SUBMISSION_ID_PARAM, String.valueOf(1))
                        .content(json.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, JWT))
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("Rating should be between 0 and 10."));
    }

    @Test
    void testSetRatingNotApproved() throws Exception {
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.LECTURER.name());
        doThrow(new IllegalStateException("Submission is not approved."))
                .when(submissionService).setRating(1, 7.5);

        JsonObject json = new JsonObject();
        json.addProperty(RATING_STRING, 7.5);

        // Perform the call
        MvcResult result = mockMvc.perform(post(SET_RATING_API)
                        .queryParam(SUBMISSION_ID_PARAM, String.valueOf(1))
                        .content(json.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, JWT))
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isForbidden())
                .andExpect(status().reason("The respective submission has not been approved"));
    }

    @Test
    void testSetRatingNoSubmission() throws Exception {
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.LECTURER.name());
        doThrow(new NoSuchElementException(NOT_FOUND_ERROR))
                .when(submissionService).setRating(1, 7.5);

        JsonObject json = new JsonObject();
        json.addProperty(RATING_STRING, 7.5);

        // Perform the call
        MvcResult result = mockMvc.perform(post(SET_RATING_API)
                        .queryParam(SUBMISSION_ID_PARAM, String.valueOf(1))
                        .content(json.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, JWT))
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetStudentLecturerPass() throws Exception {
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.LECTURER.name());
        when(submissionService.getSubmissionsForStudent(userId))
                .thenReturn(List.of(new Submission(userId, courseId, LocalDateTime.now())));

        MvcResult result = mockMvc.perform(get(GET_STUDENT_API)
                        .queryParam(USER_ID_STR, String.valueOf(userId))
                        .header(HttpHeaders.AUTHORIZATION, JWT))
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk());
    }

    @Test
    void testGetStudentTaPass() throws Exception {
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.STUDENT.name());
        when(jwtUtils.getUserId(claims)).thenReturn(userId);
        when(jwtUtils.resolveToken(JWT)).thenReturn(RESOLVED_TOKEN);
        when(jwtUtils.validateAndParseClaims(RESOLVED_TOKEN)).thenReturn(claims);
        when(submissionService.getSubmissionsForStudent(userId))
                .thenReturn(List.of(new Submission(userId, courseId, LocalDateTime.now())));

        MvcResult result = mockMvc.perform(get(GET_STUDENT_API)
                        .header(HttpHeaders.AUTHORIZATION, JWT))
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk());
    }

    @Test
    void testGetStudentLecturerForbidden() throws Exception {
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.LECTURER.name());

        MvcResult result = mockMvc.perform(get(GET_STUDENT_API)
                        .header(HttpHeaders.AUTHORIZATION, JWT))
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetStudentTaForbidden() throws Exception {
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.STUDENT.name());
        when(jwtUtils.resolveToken(JWT)).thenReturn(RESOLVED_TOKEN);
        when(jwtUtils.validateAndParseClaims(RESOLVED_TOKEN)).thenReturn(claims);
        when(submissionService.getSubmissionsForStudent(userId))
                .thenReturn(List.of(new Submission(userId, courseId, LocalDateTime.now())));

        MvcResult result = mockMvc.perform(get(GET_STUDENT_API)
                        .queryParam(USER_ID_STR, String.valueOf(userId))
                        .header(HttpHeaders.AUTHORIZATION, JWT))
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetStudentNoSubmissions() throws Exception {
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.LECTURER.name());
        when(submissionService.getSubmissionsForStudent(userId))
                .thenReturn(List.of());

        MvcResult result = mockMvc.perform(get(GET_STUDENT_API)
                        .queryParam(USER_ID_STR, String.valueOf(userId))
                        .header(HttpHeaders.AUTHORIZATION, JWT))
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isNotFound());
    }

    /**
     * Test utility function that generates a valid response for the course microservice.
     * The actual functionality of the validator that is 'fooled' here can be found in the
     * {@link nl.tudelft.sem.hiring.procedure.validation.AsyncCourseCandidacyValidatorTest} class.
     *
     * @param responseSize the size of the response (objects).
     * @return a valid response for the course microservice.
     */
    private String generateValidCourseMultiInfoMockResponse(int responseSize) {
        JsonObject json = new JsonObject();

        // Generate a valid response with random info
        for (int i = 0; i < responseSize; i++) {
            JsonObject courseInfo = new JsonObject();
            long courseId = ThreadLocalRandom.current().nextLong();
            courseInfo.addProperty("courseId", courseId);

            ZonedDateTime start = ZonedDateTime.now();
            courseInfo.addProperty(START_DATE_STR, start.toString());
            courseInfo.addProperty("endDate", start.plusMonths(3).toString());

            courseInfo.addProperty(COURSE_CODE_STR, RandomStringUtils.random(7));
            courseInfo.addProperty(NUMBER_OF_STUDENTS,
                    ThreadLocalRandom.current().nextInt(50, 500));

            json.add(String.valueOf(courseId), courseInfo);
        }

        // Return the JSON object as String
        return json.toString();
    }
}
