package nl.tudelft.sem.hiring.procedure.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import nl.tudelft.sem.hiring.procedure.entities.Application;
import nl.tudelft.sem.hiring.procedure.entities.ApplicationStatus;
import nl.tudelft.sem.hiring.procedure.services.ApplicationService;
import nl.tudelft.sem.hiring.procedure.utils.GatewayConfig;
import nl.tudelft.sem.hiring.procedure.validation.AsyncRoleValidator;
import nl.tudelft.sem.jwt.JwtUtils;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@AutoConfigureMockMvc
@SpringBootTest
public class ApplicationControllerTest {
    @Autowired
    private transient MockMvc mockMvc;

    @MockBean
    private transient ApplicationService applicationService;

    @MockBean
    private transient JwtUtils jwtUtils;

    @Mock
    private transient Jws<Claims> claims;

    @MockBean
    private transient GatewayConfig gatewayConfig;

    private static MockWebServer mockWebServer;

    private static final long courseId = 2450;
    private static final long userId = 521234;
    private static final ZonedDateTime courseStartNextYear = ZonedDateTime.now().plusYears(1);
    private static final String START_TIME = "startTime";
    private static final String BASE_URL = "/";
    private static final String RESOLVED_TOKEN = "yo";
    private static final String COURSE_ID_PARAM = "courseId=";
    private static final String USER_ID_PARAM = "userId=";
    private static final String PARAM_STARTER = "?";
    private static final String PARAM_CONTINUER = "&";
    private static final String APPLY_API = "/api/hiring-procedure/apply";
    private static final String HIRE_API = "/api/hiring-procedure/hire-TA";
    private static final String GET_APPLICATIONS_API = "/api/hiring-procedure/get-applications";
    private static final String AUTH_BODY = "Authorization";
    private static final String GET_METHOD = "GET";
    private static final String JWT = "eyJhbGciOiJIUzI1NiJ9.eyJSb2xlIjoic3R1ZGVudCIsIklz"
            + "c3VlciI6Iklzc3VlciIsIlVzZXJuYW1lIjoibXRvYWRlciIsImV4cCI6MTYzODYzNDYyMiwiaWF0Ijo"
            + "xNjM4NjM0NjIyfQ.atOFZMwAy3ERmNLmCtrxTGd1eKo1nHeTGAoM9-tXZys";

    private transient Application application
            = new Application(userId, courseId, LocalDateTime.now());

    @BeforeAll
    static void setup() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    public void testControllerNoEndpoint() throws Exception {
        mockMvc.perform(get("/api/hiring-procedure/non-existing"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testApplyEndpointPass() throws Exception {
        // Set mocks
        when(jwtUtils.resolveToken(JWT)).thenReturn(RESOLVED_TOKEN);
        when(jwtUtils.validateAndParseClaims(RESOLVED_TOKEN)).thenReturn(claims);
        when(jwtUtils.getUserId(claims)).thenReturn(userId);
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.STUDENT.name());
        when(applicationService.checkSameApplication(userId, courseId)).thenReturn(false);

        // Register listener and setup url
        HttpUrl url = mockWebServer.url(BASE_URL);
        when(gatewayConfig.getPort()).thenReturn(url.port());
        when(gatewayConfig.getHost()).thenReturn(url.host());
        JsonObject json = new JsonObject();
        json.addProperty(START_TIME, courseStartNextYear.toString());
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(json.toString()));

        // Perform the call
        MvcResult result = mockMvc.perform(post(APPLY_API
                + PARAM_STARTER + COURSE_ID_PARAM + courseId)
                        .header(AUTH_BODY, JWT))
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
            .andExpect(status().isOk());

        // Extra checks
        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        assertEquals(GET_METHOD, recordedRequest.getMethod());
    }

    @Test
    public void testApplyEndpointUserNotStudent() throws Exception {
        // Set mocks
        when(jwtUtils.resolveToken(JWT)).thenReturn(RESOLVED_TOKEN);
        when(jwtUtils.validateAndParseClaims(RESOLVED_TOKEN)).thenReturn(claims);
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.LECTURER.name());

        // Perform the call
        MvcResult result = mockMvc.perform(post(APPLY_API
                + PARAM_STARTER + COURSE_ID_PARAM + courseId)
                        .header(AUTH_BODY, JWT))
                .andReturn();
        mockMvc.perform(asyncDispatch(result))
            .andExpect(status().isForbidden());
    }

    @Test
    public void testApplyEndpointDeadlinePassed() throws Exception {
        // Set mocks
        when(jwtUtils.resolveToken(JWT)).thenReturn(RESOLVED_TOKEN);
        when(jwtUtils.validateAndParseClaims(RESOLVED_TOKEN)).thenReturn(claims);
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.STUDENT.name());

        // Register listener and setup url
        HttpUrl url = mockWebServer.url(BASE_URL);
        when(gatewayConfig.getPort()).thenReturn(url.port());
        when(gatewayConfig.getHost()).thenReturn(url.host());
        JsonObject json = new JsonObject();
        json.addProperty(START_TIME, ZonedDateTime.now()
                .plusWeeks(1).toString());
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(json.toString()));

        // Perform the call
        MvcResult result = mockMvc.perform(post(APPLY_API
                + PARAM_STARTER + COURSE_ID_PARAM + courseId)
                        .header(AUTH_BODY, JWT))
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
        when(jwtUtils.resolveToken(JWT)).thenReturn(RESOLVED_TOKEN);
        when(jwtUtils.validateAndParseClaims(RESOLVED_TOKEN)).thenReturn(claims);
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.STUDENT.name());
        when(jwtUtils.getUserId(claims)).thenReturn(userId);
        when(applicationService.checkSameApplication(userId, courseId)).thenReturn(true);

        // Register listener and setup url
        HttpUrl url = mockWebServer.url(BASE_URL);
        when(gatewayConfig.getPort()).thenReturn(url.port());
        when(gatewayConfig.getHost()).thenReturn(url.host());
        JsonObject json = new JsonObject();
        json.addProperty(START_TIME, courseStartNextYear.toString());
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(json.toString()));

        // Perform the call
        MvcResult result = mockMvc.perform(post(APPLY_API
                + PARAM_STARTER + COURSE_ID_PARAM + courseId)
                        .header(AUTH_BODY, JWT))
                        .andReturn();

        mockMvc.perform(asyncDispatch(result))
            .andExpect(status().isForbidden());

        // Extra checks
        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        assertEquals(GET_METHOD, recordedRequest.getMethod());
    }

    @Test
    public void testApplyEndpointCourseNotFound() throws Exception {
        // Set mocks
        when(jwtUtils.resolveToken(JWT)).thenReturn(RESOLVED_TOKEN);
        when(jwtUtils.validateAndParseClaims(RESOLVED_TOKEN)).thenReturn(claims);
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.STUDENT.name());

        // Register listener and setup url
        HttpUrl url = mockWebServer.url(BASE_URL);
        when(gatewayConfig.getPort()).thenReturn(url.port());
        when(gatewayConfig.getHost()).thenReturn(url.host());
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404));

        // Perform the call
        MvcResult result = mockMvc.perform(post(APPLY_API
                + PARAM_STARTER + COURSE_ID_PARAM + courseId)
                        .header(AUTH_BODY, JWT))
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
                        .header(AUTH_BODY, JWT))
                        .andReturn();

        mockMvc.perform(asyncDispatch(result))
            .andExpect(status().isForbidden());
    }

    @Test
    public void testGetAllEndpointPass() throws Exception {
        // Set mocks
        when(jwtUtils.resolveToken(JWT)).thenReturn(RESOLVED_TOKEN);
        when(jwtUtils.validateAndParseClaims(RESOLVED_TOKEN)).thenReturn(claims);
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.LECTURER.name());

        // Perform the call
        MvcResult result = mockMvc.perform(get("/api/hiring-procedure/get-all-applications")
                        .header(AUTH_BODY, JWT))
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
            .andExpect(status().isOk());
    }

    @Test
    public void testGetAllEndpointNotLecturer() throws Exception {
        // Set mocks
        when(jwtUtils.resolveToken(JWT)).thenReturn(RESOLVED_TOKEN);
        when(jwtUtils.validateAndParseClaims(RESOLVED_TOKEN)).thenReturn(claims);
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.STUDENT.name());

        // Perform the call
        MvcResult result = mockMvc.perform(get("/api/hiring-procedure/get-all-applications")
                .header(AUTH_BODY, JWT))
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
        MvcResult result = mockMvc.perform(get("/api/hiring-procedure/get-all-applications")
                .header(AUTH_BODY, JWT))
            .andReturn();

        mockMvc.perform(asyncDispatch(result))
            .andExpect(status().isForbidden());
    }

    @Test
    public void testGetApplicationsEndpointPass() throws Exception {
        // Set mocks
        when(jwtUtils.resolveToken(JWT)).thenReturn(RESOLVED_TOKEN);
        when(jwtUtils.validateAndParseClaims(RESOLVED_TOKEN)).thenReturn(claims);
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.LECTURER.name());

        // Perform the call
        MvcResult result = mockMvc.perform(get(GET_APPLICATIONS_API + PARAM_STARTER
                        + COURSE_ID_PARAM + courseId)
                        .header(AUTH_BODY, JWT))
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
            .andExpect(status().isOk());
    }

    @Test
    public void testGetApplicationsEndpointNotLecturer() throws Exception {
        // Set mocks
        when(jwtUtils.resolveToken(JWT)).thenReturn(RESOLVED_TOKEN);
        when(jwtUtils.validateAndParseClaims(RESOLVED_TOKEN)).thenReturn(claims);
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.STUDENT.name());

        // Perform the call
        MvcResult result = mockMvc.perform(get(GET_APPLICATIONS_API + PARAM_STARTER
                + COURSE_ID_PARAM + courseId)
                .header(AUTH_BODY, JWT))
            .andReturn();

        mockMvc.perform(asyncDispatch(result))
            .andExpect(status().isForbidden());
    }

    @Test
    public void testGetApplicationsEndpointInvalidToken() throws Exception {
        // Set mocks
        when(jwtUtils.resolveToken(JWT)).thenReturn(RESOLVED_TOKEN);
        when(jwtUtils.validateAndParseClaims(RESOLVED_TOKEN)).thenReturn(null);

        // Perform the call
        MvcResult result = mockMvc.perform(get(GET_APPLICATIONS_API + PARAM_STARTER
                + COURSE_ID_PARAM + courseId)
                .header(AUTH_BODY, JWT))
            .andReturn();

        mockMvc.perform(asyncDispatch(result))
            .andExpect(status().isForbidden());
    }

    @Test
    public void testHireEndpointPass() throws Exception {
        // Set mocks
        when(jwtUtils.resolveToken(JWT)).thenReturn(RESOLVED_TOKEN);
        when(jwtUtils.validateAndParseClaims(RESOLVED_TOKEN)).thenReturn(claims);
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.LECTURER.name());
        when(applicationService.checkCandidate(userId, courseId)).thenReturn(true);
        when(applicationService.getApplication(userId, courseId))
                .thenReturn(Optional.of(application));

        // Register listener and setup url
        HttpUrl url = mockWebServer.url(BASE_URL);
        when(gatewayConfig.getPort()).thenReturn(url.port());
        when(gatewayConfig.getHost()).thenReturn(url.host());
        JsonObject json = new JsonObject();
        json.addProperty(START_TIME, courseStartNextYear.toString());
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(json.toString()));
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200));

        // Perform the call
        MvcResult result = mockMvc.perform(post(HIRE_API)
                        .param("userId", String.valueOf(userId))
                        .param("course" + "Id", String.valueOf(courseId))
                        .header(AUTH_BODY, JWT))
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
    }

    @Test
    public void testHireEndpointApplicationNotFoundFailed() throws Exception {
        // Set mocks
        when(jwtUtils.resolveToken(JWT)).thenReturn(RESOLVED_TOKEN);
        when(jwtUtils.validateAndParseClaims(RESOLVED_TOKEN)).thenReturn(claims);
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.LECTURER.name());
        when(applicationService.checkCandidate(userId, courseId)).thenReturn(true);

        // Register listener and setup url
        HttpUrl url = mockWebServer.url(BASE_URL);
        when(gatewayConfig.getPort()).thenReturn(url.port());
        when(gatewayConfig.getHost()).thenReturn(url.host());
        JsonObject json = new JsonObject();
        json.addProperty(START_TIME, courseStartNextYear.toString());
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(json.toString()));
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200));

        // Perform the call
        MvcResult result = mockMvc.perform(post(HIRE_API)
                        .param("userId", String.valueOf(userId))
                        .param("course" + "Id", String.valueOf(courseId))
                        .header(AUTH_BODY, JWT))
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
    }

    @Test
    public void testHireEndpointInvalidToken() throws Exception {
        // Set mocks
        when(jwtUtils.resolveToken(JWT)).thenReturn(RESOLVED_TOKEN);
        when(jwtUtils.validateAndParseClaims(RESOLVED_TOKEN)).thenReturn(null);

        // Perform the call
        MvcResult result = mockMvc.perform(post(HIRE_API
                + PARAM_STARTER + USER_ID_PARAM + userId
                + PARAM_CONTINUER + COURSE_ID_PARAM + courseId)
                .header(AUTH_BODY, JWT))
            .andReturn();

        mockMvc.perform(asyncDispatch(result))
            .andExpect(status().isForbidden());
    }

    @Test
    public void testHireEndpointNotLecturer() throws Exception {
        // Set mocks
        when(jwtUtils.resolveToken(JWT)).thenReturn(RESOLVED_TOKEN);
        when(jwtUtils.validateAndParseClaims(RESOLVED_TOKEN)).thenReturn(claims);
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.STUDENT.name());

        // Perform the call
        MvcResult result = mockMvc.perform(post(HIRE_API
                + PARAM_STARTER + USER_ID_PARAM + userId
                + PARAM_CONTINUER + COURSE_ID_PARAM + courseId)
                .header(AUTH_BODY, JWT))
            .andReturn();

        mockMvc.perform(asyncDispatch(result))
            .andExpect(status().isForbidden());
    }

    @Test
    public void testHireEndpointCourseNotFound() throws Exception {
        // Set mocks
        when(jwtUtils.resolveToken(JWT)).thenReturn(RESOLVED_TOKEN);
        when(jwtUtils.validateAndParseClaims(RESOLVED_TOKEN)).thenReturn(claims);
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.LECTURER.name());

        // Register listener and setup url
        HttpUrl url = mockWebServer.url(BASE_URL);
        when(gatewayConfig.getPort()).thenReturn(url.port());
        when(gatewayConfig.getHost()).thenReturn(url.host());
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody("Course not found."));

        // Perform the call
        MvcResult result = mockMvc.perform(post(HIRE_API
                + PARAM_STARTER + USER_ID_PARAM + userId
                + PARAM_CONTINUER + COURSE_ID_PARAM + courseId)
                .header(AUTH_BODY, JWT))
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
        when(jwtUtils.resolveToken(JWT)).thenReturn(RESOLVED_TOKEN);
        when(jwtUtils.validateAndParseClaims(RESOLVED_TOKEN)).thenReturn(claims);
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.LECTURER.name());

        // Register listener and setup url
        HttpUrl url = mockWebServer.url(BASE_URL);
        when(gatewayConfig.getPort()).thenReturn(url.port());
        when(gatewayConfig.getHost()).thenReturn(url.host());
        JsonObject json = new JsonObject();
        json.addProperty(START_TIME, courseStartNextYear.toString());
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(json.toString()));
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404));

        // Perform the call
        MvcResult result = mockMvc.perform(post(HIRE_API
                + PARAM_STARTER + USER_ID_PARAM + userId
                + PARAM_CONTINUER + COURSE_ID_PARAM + courseId)
                .header(AUTH_BODY, JWT))
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
    }

    @Test
    public void testHireEndpointNotViable() throws Exception {
        // Set mocks
        when(jwtUtils.resolveToken(JWT)).thenReturn(RESOLVED_TOKEN);
        when(jwtUtils.validateAndParseClaims(RESOLVED_TOKEN)).thenReturn(claims);
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.LECTURER.name());
        when(applicationService.checkCandidate(userId, courseId)).thenReturn(false);

        // Register listener and setup url
        HttpUrl url = mockWebServer.url(BASE_URL);
        when(gatewayConfig.getPort()).thenReturn(url.port());
        when(gatewayConfig.getHost()).thenReturn(url.host());
        JsonObject json = new JsonObject();
        json.addProperty(START_TIME, courseStartNextYear.toString());
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(json.toString()));
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200));

        // Perform the call
        MvcResult result = mockMvc.perform(post(HIRE_API
                + PARAM_STARTER + USER_ID_PARAM + userId
                + PARAM_CONTINUER + COURSE_ID_PARAM + courseId)
                .header(AUTH_BODY, JWT))
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
        // Configure the mocks
        HttpUrl url = mockWebServer.url(BASE_URL);
        when(gatewayConfig.getPort()).thenReturn(url.port());
        when(gatewayConfig.getHost()).thenReturn(url.host());

        // Create new application
        ZonedDateTime start = ZonedDateTime.now();
        Application application = new Application(userId, courseId, start.toLocalDateTime());
        when(applicationService.getApplication(userId, courseId))
                .thenReturn(Optional.of(application));

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
                        .queryParam("courseId", String.valueOf(courseId)))
                .andReturn();

        // Await the call
        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk());

        // Verify that there was an attempt to change the application status
        verify(applicationService, times(1)).withdrawApplication(application.getApplicationId());
    }

    @Test
    void testWithdrawNonExisting() throws Exception {
        // Configure the mocks
        HttpUrl url = mockWebServer.url(BASE_URL);
        when(gatewayConfig.getPort()).thenReturn(url.port());
        when(gatewayConfig.getHost()).thenReturn(url.host());

        // Create new application mock behaviour
        when(applicationService.getApplication(userId, courseId)).thenReturn(Optional.empty());

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
                        .queryParam("courseId", String.valueOf(courseId)))
                .andReturn();

        // Await the call
        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isMethodNotAllowed());

        // Verify that there was no attempt to change the application status
        verify(applicationService, times(0)).withdrawApplication(Mockito.anyLong());
    }

    @Test
    void testWithdrawAlreadyProcessed() throws Exception {
        // Configure the mocks
        HttpUrl url = mockWebServer.url("/");
        when(gatewayConfig.getPort()).thenReturn(url.port());
        when(gatewayConfig.getHost()).thenReturn(url.host());

        // Create new application
        ZonedDateTime start = ZonedDateTime.now();
        Application application = new Application(userId, courseId, start.toLocalDateTime());
        application.setStatus(ApplicationStatus.ACCEPTED);
        when(applicationService.getApplication(userId, courseId))
                .thenReturn(Optional.of(application));

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
                        .queryParam("courseId", String.valueOf(courseId)))
                .andReturn();

        // Await the call
        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isMethodNotAllowed());

        // Verify that there was no attempt to change the application status
        verify(applicationService, times(0)).withdrawApplication(application.getApplicationId());
    }

    @Test
    void testReject() throws Exception {
        // Configure the mocks
        HttpUrl url = mockWebServer.url("/");
        when(gatewayConfig.getPort()).thenReturn(url.port());
        when(gatewayConfig.getHost()).thenReturn(url.host());

        // Application info
        long applicationId = 1337L;
        Application applicationMock = Mockito.mock(Application.class);
        when(applicationService.getApplication(applicationId))
                .thenReturn(Optional.of(applicationMock));
        when(applicationMock.getStatus()).thenReturn(ApplicationStatus.IN_PROGRESS);

        // Configure request mock
        when(jwtUtils.getUserId(Mockito.any())).thenReturn(userId);
        when(jwtUtils.resolveToken(Mockito.any())).thenReturn("");
        when(jwtUtils.validateAndParseClaims(Mockito.any())).thenReturn(claims);
        when(jwtUtils.getRole(Mockito.any())).thenReturn(AsyncRoleValidator.Roles.LECTURER.name());

        // Create request body and perform the call
        MvcResult result = mockMvc.perform(post("/api/hiring-procedure/reject")
                        .header(HttpHeaders.AUTHORIZATION, "")
                        .queryParam("applicationId", String.valueOf(applicationId)))
                .andReturn();

        // Await the call
        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk());

        // Verify that there was an attempt to change the application status
        verify(applicationService, times(1)).getApplication(applicationId);
        verify(applicationService, times(1)).rejectApplication(applicationId);
    }


    @Test
    void testRejectNonExisting() throws Exception {
        // Configure the mocks
        HttpUrl url = mockWebServer.url("/");
        when(gatewayConfig.getPort()).thenReturn(url.port());
        when(gatewayConfig.getHost()).thenReturn(url.host());

        // Application info
        long applicationId = 1337L;
        when(applicationService.getApplication(applicationId)).thenReturn(Optional.empty());

        // Configure request mock
        when(jwtUtils.getUserId(Mockito.any())).thenReturn(userId);
        when(jwtUtils.resolveToken(Mockito.any())).thenReturn("");
        when(jwtUtils.validateAndParseClaims(Mockito.any())).thenReturn(claims);
        when(jwtUtils.getRole(Mockito.any())).thenReturn(AsyncRoleValidator.Roles.LECTURER.name());

        // Create request body and perform the call
        MvcResult result = mockMvc.perform(post("/api/hiring-procedure/reject")
                        .header(HttpHeaders.AUTHORIZATION, "")
                        .queryParam("applicationId", String.valueOf(applicationId)))
                .andReturn();

        // Await the call
        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isNotFound());

        // Verify that there was an attempt to fetch the application
        verify(applicationService, times(1)).getApplication(applicationId);
        verify(applicationService, times(0)).rejectApplication(applicationId);
    }

    @Test
    void testRejectAlreadyProcessed() throws Exception {
        // Configure the mocks
        HttpUrl url = mockWebServer.url("/");
        when(gatewayConfig.getPort()).thenReturn(url.port());
        when(gatewayConfig.getHost()).thenReturn(url.host());

        // Application info
        long applicationId = 1337L;
        Application applicationMock = Mockito.mock(Application.class);
        when(applicationService.getApplication(applicationId))
                .thenReturn(Optional.of(applicationMock));
        when(applicationMock.getStatus()).thenReturn(ApplicationStatus.ACCEPTED);

        // Configure request mock
        when(jwtUtils.getUserId(Mockito.any())).thenReturn(userId);
        when(jwtUtils.resolveToken(Mockito.any())).thenReturn("");
        when(jwtUtils.validateAndParseClaims(Mockito.any())).thenReturn(claims);
        when(jwtUtils.getRole(Mockito.any())).thenReturn(AsyncRoleValidator.Roles.LECTURER.name());

        // Create request body and perform the call
        MvcResult result = mockMvc.perform(post("/api/hiring-procedure/reject")
                        .header(HttpHeaders.AUTHORIZATION, "")
                        .queryParam("applicationId", String.valueOf(applicationId)))
                .andReturn();

        // Await the call
        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isMethodNotAllowed());

        // Verify that there was an attempt to change the application status
        verify(applicationService, times(1)).getApplication(applicationId);
        verify(applicationService, times(0)).rejectApplication(applicationId);
    }

    @Test
    void testGetOwnContractSuccess() throws Exception {
        when(jwtUtils.resolveToken(JWT)).thenReturn(RESOLVED_TOKEN);
        when(jwtUtils.validateAndParseClaims(RESOLVED_TOKEN)).thenReturn(claims);
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.STUDENT.name());

        // Perform the call
        MvcResult result = mockMvc.perform(get("/api/hiring-procedure/"
                + "get-contract?courseId=" + courseId)
                .header(AUTH_BODY, JWT))
            .andReturn();

        mockMvc.perform(asyncDispatch(result))
            .andExpect(status().isOk());
    }

    @Test
    void testGetContractSuccess() throws Exception {
        when(jwtUtils.resolveToken(JWT)).thenReturn(RESOLVED_TOKEN);
        when(jwtUtils.validateAndParseClaims(RESOLVED_TOKEN)).thenReturn(claims);
        when(jwtUtils.getRole(claims)).thenReturn(AsyncRoleValidator.Roles.LECTURER.name());

        // Perform the call
        MvcResult result = mockMvc.perform(get("/api/hiring-procedure/"
                + "get-contract?userId=" + userId + "&courseId=" + courseId)
                .header(AUTH_BODY, JWT))
            .andReturn();

        mockMvc.perform(asyncDispatch(result))
            .andExpect(status().isOk());
    }

}
