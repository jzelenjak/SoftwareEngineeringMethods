package nl.tudelft.sem.hiring.procedure.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import nl.tudelft.sem.hiring.procedure.services.ApplicationService;
import nl.tudelft.sem.hiring.procedure.utils.GatewayConfig;
import nl.tudelft.sem.jwt.JwtUtils;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.web.servlet.MockMvc;

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

    private final transient long courseId = 2450;
    private final transient long userId = 521234;
    private final transient LocalDateTime courseStartNextYear = LocalDateTime.now().plusYears(1);
    private static final String RESOLVED_TOKEN = "yo";
    private static final String STUDENT_ROLE = "student";
    private static final String LECTURER_ROLE = "lecturer";
    private static final String COURSES_TARGET = "get-start-date";
    private static final String COURSES_API = "/api/courses/";
    private static final String COURSE_ID_PARAM = "courseId=";
    private static final String USER_ID_PARAM = "userId=";
    private static final String PARAM_STARTER = "?";
    private static final String PARAM_CONTINUER = "&";
    private static final String BODY_START = "{\n  \"courseStartDate\": \"";
    private static final String BODY_END = "\"\n}";
    private static final String APPLY_API = "/api/hiring-procedure/apply";
    private static final String HIRE_API = "/api/hiring-procedure/hire-TA";
    private static final String GET_APPLICATIONS_API = "/api/hiring-procedure/get-applications";
    private static final String AUTH_BODY = "Authorization";
    private static final String GET_METHOD = "GET";
    private static final String JWT = "eyJhbGciOiJIUzI1NiJ9.eyJSb2xlIjoic3R1ZGVudCIsIklz"
        + "c3VlciI6Iklzc3VlciIsIlVzZXJuYW1lIjoibXRvYWRlciIsImV4cCI6MTYzODYzNDYyMiwiaWF0Ijo"
        + "xNjM4NjM0NjIyfQ.atOFZMwAy3ERmNLmCtrxTGd1eKo1nHeTGAoM9-tXZys";

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
    public void controllerNoEndpointTest() throws Exception {
        mockMvc.perform(get("/api/hiring-procedure/non-existing"))
            .andExpect(status().isNotFound());
    }

    @Test
    public void applyEndpointTestPass() throws Exception {
        // Set mocks
        when(jwtUtils.resolveToken(JWT)).thenReturn(RESOLVED_TOKEN);
        when(jwtUtils.validateAndParseClaims(RESOLVED_TOKEN)).thenReturn(claims);
        when(jwtUtils.getRole(claims)).thenReturn(STUDENT_ROLE);
        when(jwtUtils.getUserId(claims)).thenReturn(userId);
        when(applicationService.checkDeadline(courseStartNextYear)).thenReturn(true);
        when(applicationService.checkSameApplication(userId, courseId)).thenReturn(false);

        // Register listener and setup url
        HttpUrl url = mockWebServer.url(COURSES_API + COURSES_TARGET + PARAM_STARTER
            + COURSE_ID_PARAM + courseId);
        when(gatewayConfig.getPort()).thenReturn(url.port());
        when(gatewayConfig.getHost()).thenReturn(url.host());
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody(BODY_START + courseStartNextYear + BODY_END));

        // Perform the call
        mockMvc.perform(post(APPLY_API + PARAM_STARTER + COURSE_ID_PARAM + courseId)
                .header(AUTH_BODY, JWT))
            .andExpect(status().isOk())
            .andReturn();

        // Extra checks
        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        assertEquals(GET_METHOD, recordedRequest.getMethod());
    }

    @Test
    public void applyEndpointUserNotStudent() throws Exception {
        // Set mocks
        when(jwtUtils.resolveToken(JWT)).thenReturn(RESOLVED_TOKEN);
        when(jwtUtils.validateAndParseClaims(RESOLVED_TOKEN)).thenReturn(claims);
        when(jwtUtils.getRole(claims)).thenReturn(LECTURER_ROLE);

        // Perform the call
        mockMvc.perform(post(APPLY_API + PARAM_STARTER + COURSE_ID_PARAM + courseId)
                .header(AUTH_BODY, JWT))
            .andExpect(status().isMethodNotAllowed())
            .andReturn();
    }

    @Test
    public void applyEndpointDeadlinePassed() throws Exception {
        // Set mocks
        when(jwtUtils.resolveToken(JWT)).thenReturn(RESOLVED_TOKEN);
        when(jwtUtils.validateAndParseClaims(RESOLVED_TOKEN)).thenReturn(claims);
        when(jwtUtils.getRole(claims)).thenReturn(STUDENT_ROLE);
        when(applicationService.checkDeadline(courseStartNextYear)).thenReturn(false);

        // Register listener and setup url
        HttpUrl url = mockWebServer.url(COURSES_API + COURSES_TARGET + PARAM_STARTER
            + COURSE_ID_PARAM + courseId);
        when(gatewayConfig.getPort()).thenReturn(url.port());
        when(gatewayConfig.getHost()).thenReturn(url.host());
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody(BODY_START + courseStartNextYear + BODY_END));

        // Perform the call
        mockMvc.perform(post(APPLY_API + PARAM_STARTER + COURSE_ID_PARAM + courseId)
                .header(AUTH_BODY, JWT))
            .andExpect(status().isMethodNotAllowed())
            .andReturn();

        // Extra checks
        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        assertEquals(GET_METHOD, recordedRequest.getMethod());
    }

    @Test
    public void applyEndpointUserApplied() throws Exception {
        // Set mocks
        when(jwtUtils.resolveToken(JWT)).thenReturn(RESOLVED_TOKEN);
        when(jwtUtils.validateAndParseClaims(RESOLVED_TOKEN)).thenReturn(claims);
        when(jwtUtils.getRole(claims)).thenReturn(STUDENT_ROLE);
        when(jwtUtils.getUserId(claims)).thenReturn(userId);
        when(applicationService.checkDeadline(courseStartNextYear)).thenReturn(true);
        when(applicationService.checkSameApplication(userId, courseId)).thenReturn(true);

        // Register listener and setup url
        HttpUrl url = mockWebServer.url(COURSES_API + COURSES_TARGET + PARAM_STARTER
            + COURSE_ID_PARAM + courseId);
        when(gatewayConfig.getPort()).thenReturn(url.port());
        when(gatewayConfig.getHost()).thenReturn(url.host());
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody(BODY_START + courseStartNextYear + BODY_END));

        // Perform the call
        mockMvc.perform(post(APPLY_API + PARAM_STARTER + COURSE_ID_PARAM + courseId)
                .header(AUTH_BODY, JWT))
            .andExpect(status().isMethodNotAllowed())
            .andReturn();

        // Extra checks
        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        assertEquals(GET_METHOD, recordedRequest.getMethod());
    }

    @Test
    public void applyEndpointCourseNotFound() throws Exception {
        // Set mocks
        when(jwtUtils.resolveToken(JWT)).thenReturn(RESOLVED_TOKEN);
        when(jwtUtils.validateAndParseClaims(RESOLVED_TOKEN)).thenReturn(claims);
        when(jwtUtils.getRole(claims)).thenReturn(STUDENT_ROLE);

        // Register listener and setup url
        HttpUrl url = mockWebServer.url(COURSES_API + COURSES_TARGET + PARAM_STARTER
            + COURSE_ID_PARAM + courseId);
        when(gatewayConfig.getPort()).thenReturn(url.port());
        when(gatewayConfig.getHost()).thenReturn(url.host());
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(404)
            .setBody("Not found"));

        // Perform the call
        mockMvc.perform(post(APPLY_API + PARAM_STARTER + COURSE_ID_PARAM + courseId)
                .header(AUTH_BODY, JWT))
            .andExpect(status().isNotFound())
            .andReturn();

        // Extra checks
        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        assertEquals(GET_METHOD, recordedRequest.getMethod());
    }

    @Test
    public void applyEndpointInvalidToken() throws Exception {
        // Set mocks
        when(jwtUtils.resolveToken(JWT)).thenReturn(RESOLVED_TOKEN);
        when(jwtUtils.validateAndParseClaims(RESOLVED_TOKEN)).thenReturn(null);

        // Perform the call
        mockMvc.perform(post(APPLY_API + PARAM_STARTER + COURSE_ID_PARAM + courseId)
                .header(AUTH_BODY, JWT))
            .andExpect(status().isNotFound())
            .andReturn();
    }

    @Test
    public void getAllEndpointPass() throws Exception {
        // Set mocks
        when(jwtUtils.resolveToken(JWT)).thenReturn(RESOLVED_TOKEN);
        when(jwtUtils.validateAndParseClaims(RESOLVED_TOKEN)).thenReturn(claims);
        when(jwtUtils.getRole(claims)).thenReturn(LECTURER_ROLE);

        // Perform the call
        mockMvc.perform(get("/api/hiring-procedure/get-all-applications")
                .header(AUTH_BODY, JWT))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void getAllEndpointNotLecturer() throws Exception {
        // Set mocks
        when(jwtUtils.resolveToken(JWT)).thenReturn(RESOLVED_TOKEN);
        when(jwtUtils.validateAndParseClaims(RESOLVED_TOKEN)).thenReturn(claims);
        when(jwtUtils.getRole(claims)).thenReturn(STUDENT_ROLE);

        // Perform the call
        mockMvc.perform(get("/api/hiring-procedure/get-all-applications")
                .header(AUTH_BODY, JWT))
            .andExpect(status().isMethodNotAllowed())
            .andReturn();
    }

    @Test
    public void getAllEndpointInvalidToken() throws Exception {
        // Set mocks
        when(jwtUtils.resolveToken(JWT)).thenReturn(RESOLVED_TOKEN);
        when(jwtUtils.validateAndParseClaims(RESOLVED_TOKEN)).thenReturn(null);
        // Perform the call
        mockMvc.perform(get("/api/hiring-procedure/get-all-applications")
                .header(AUTH_BODY, JWT))
            .andExpect(status().isNotFound())
            .andReturn();
    }

    @Test
    public void getApplicationsEndpointPass() throws Exception {
        // Set mocks
        when(jwtUtils.resolveToken(JWT)).thenReturn(RESOLVED_TOKEN);
        when(jwtUtils.validateAndParseClaims(RESOLVED_TOKEN)).thenReturn(claims);
        when(jwtUtils.getRole(claims)).thenReturn(LECTURER_ROLE);

        // Register listener and setup url
        HttpUrl url = mockWebServer.url(COURSES_API + COURSES_TARGET + PARAM_STARTER
            + COURSE_ID_PARAM + courseId);
        when(gatewayConfig.getPort()).thenReturn(url.port());
        when(gatewayConfig.getHost()).thenReturn(url.host());
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody(BODY_START + courseStartNextYear + BODY_END));

        // Perform the call
        mockMvc.perform(get(GET_APPLICATIONS_API + PARAM_STARTER
                + COURSE_ID_PARAM + courseId)
                .header(AUTH_BODY, JWT))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void getApplicationsEndpointNotLecturer() throws Exception {
        // Set mocks
        when(jwtUtils.resolveToken(JWT)).thenReturn(RESOLVED_TOKEN);
        when(jwtUtils.validateAndParseClaims(RESOLVED_TOKEN)).thenReturn(claims);
        when(jwtUtils.getRole(claims)).thenReturn(STUDENT_ROLE);

        // Perform the call
        mockMvc.perform(get(GET_APPLICATIONS_API + PARAM_STARTER
                + COURSE_ID_PARAM + courseId)
                .header(AUTH_BODY, JWT))
            .andExpect(status().isMethodNotAllowed())
            .andReturn();
    }

    @Test
    public void getApplicationsEndpointInvalidToken() throws Exception {
        // Set mocks
        when(jwtUtils.resolveToken(JWT)).thenReturn(RESOLVED_TOKEN);
        when(jwtUtils.validateAndParseClaims(RESOLVED_TOKEN)).thenReturn(null);

        // Perform the call
        mockMvc.perform(get(GET_APPLICATIONS_API + PARAM_STARTER
                + COURSE_ID_PARAM + courseId)
                .header(AUTH_BODY, JWT))
            .andExpect(status().isNotFound())
            .andReturn();
    }

    @Test
    public void getApplicationsNoCourse() throws Exception {
        // Set mocks
        when(jwtUtils.resolveToken(JWT)).thenReturn(RESOLVED_TOKEN);
        when(jwtUtils.validateAndParseClaims(RESOLVED_TOKEN)).thenReturn(claims);
        when(jwtUtils.getRole(claims)).thenReturn(LECTURER_ROLE);

        // Register listener and setup url
        HttpUrl url = mockWebServer.url(COURSES_API + COURSES_TARGET + PARAM_STARTER
            + COURSE_ID_PARAM + courseId);
        when(gatewayConfig.getPort()).thenReturn(url.port());
        when(gatewayConfig.getHost()).thenReturn(url.host());
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(404)
            .setBody("Course not found"));

        // Perform the call
        mockMvc.perform(get(GET_APPLICATIONS_API + PARAM_STARTER
                + COURSE_ID_PARAM + courseId)
                .header(AUTH_BODY, JWT))
            .andExpect(status().isNotFound())
            .andReturn();
    }

    @Test
    public void hireEndpointTestPass() throws Exception {
        // Set mocks
        when(jwtUtils.resolveToken(JWT)).thenReturn(RESOLVED_TOKEN);
        when(jwtUtils.validateAndParseClaims(RESOLVED_TOKEN)).thenReturn(claims);
        when(jwtUtils.getRole(claims)).thenReturn(LECTURER_ROLE);
        when(applicationService.checkCandidate(userId, courseId)).thenReturn(true);

        // Register listener and setup url
        HttpUrl url = mockWebServer.url(COURSES_API + COURSES_TARGET + PARAM_STARTER
            + COURSE_ID_PARAM + courseId);
        when(gatewayConfig.getPort()).thenReturn(url.port());
        when(gatewayConfig.getHost()).thenReturn(url.host());
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody(BODY_START + courseStartNextYear + BODY_END));
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(200));

        // Perform the call
        mockMvc.perform(post(HIRE_API + PARAM_STARTER + USER_ID_PARAM + userId
                + PARAM_CONTINUER + COURSE_ID_PARAM + courseId)
                .header(AUTH_BODY, JWT))
            .andExpect(status().isOk())
            .andReturn();

        // Extra checks
        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        assertEquals(GET_METHOD, recordedRequest.getMethod());
        recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        assertEquals(GET_METHOD, recordedRequest.getMethod());
    }

    @Test
    public void hireEndpointInvalidToken() throws Exception {
        // Set mocks
        when(jwtUtils.resolveToken(JWT)).thenReturn(RESOLVED_TOKEN);
        when(jwtUtils.validateAndParseClaims(RESOLVED_TOKEN)).thenReturn(null);

        // Perform the call
        mockMvc.perform(post(HIRE_API + PARAM_STARTER + USER_ID_PARAM + userId
                + PARAM_CONTINUER + COURSE_ID_PARAM + courseId)
                .header(AUTH_BODY, JWT))
            .andExpect(status().isNotFound())
            .andReturn();
    }

    @Test
    public void hireEndpointNotLecturer() throws Exception {
        // Set mocks
        when(jwtUtils.resolveToken(JWT)).thenReturn(RESOLVED_TOKEN);
        when(jwtUtils.validateAndParseClaims(RESOLVED_TOKEN)).thenReturn(claims);
        when(jwtUtils.getRole(claims)).thenReturn(STUDENT_ROLE);

        // Perform the call
        mockMvc.perform(post(HIRE_API + PARAM_STARTER + USER_ID_PARAM + userId
                + PARAM_CONTINUER + COURSE_ID_PARAM + courseId)
                .header(AUTH_BODY, JWT))
            .andExpect(status().isMethodNotAllowed())
            .andReturn();
    }

    @Test
    public void hireEndpointCourseNotFound() throws Exception {
        // Set mocks
        when(jwtUtils.resolveToken(JWT)).thenReturn(RESOLVED_TOKEN);
        when(jwtUtils.validateAndParseClaims(RESOLVED_TOKEN)).thenReturn(claims);
        when(jwtUtils.getRole(claims)).thenReturn(LECTURER_ROLE);

        // Register listener and setup url
        HttpUrl url = mockWebServer.url(COURSES_API + COURSES_TARGET + PARAM_STARTER
            + COURSE_ID_PARAM + courseId);
        when(gatewayConfig.getPort()).thenReturn(url.port());
        when(gatewayConfig.getHost()).thenReturn(url.host());
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(404)
            .setBody("Course not found."));

        // Perform the call
        mockMvc.perform(post(HIRE_API + PARAM_STARTER + USER_ID_PARAM + userId
                + PARAM_CONTINUER + COURSE_ID_PARAM + courseId)
                .header(AUTH_BODY, JWT))
            .andExpect(status().isNotFound())
            .andReturn();

        // Extra checks
        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        assertEquals(GET_METHOD, recordedRequest.getMethod());
    }

    @Test
    public void hireEndpointUserNotFound() throws Exception {
        // Set mocks
        when(jwtUtils.resolveToken(JWT)).thenReturn(RESOLVED_TOKEN);
        when(jwtUtils.validateAndParseClaims(RESOLVED_TOKEN)).thenReturn(claims);
        when(jwtUtils.getRole(claims)).thenReturn(LECTURER_ROLE);

        // Register listener and setup url
        HttpUrl url = mockWebServer.url(COURSES_API + COURSES_TARGET + PARAM_STARTER
            + COURSE_ID_PARAM + courseId);
        when(gatewayConfig.getPort()).thenReturn(url.port());
        when(gatewayConfig.getHost()).thenReturn(url.host());
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody(BODY_START + courseStartNextYear + BODY_END));
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(404));

        // Perform the call
        mockMvc.perform(post(HIRE_API + PARAM_STARTER + USER_ID_PARAM + userId
                + PARAM_CONTINUER + COURSE_ID_PARAM + courseId)
                .header(AUTH_BODY, JWT))
            .andExpect(status().isNotFound())
            .andReturn();

        // Extra checks
        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        assertEquals(GET_METHOD, recordedRequest.getMethod());
        recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        assertEquals(GET_METHOD, recordedRequest.getMethod());
    }

    @Test
    public void hireEndpointNotViable() throws Exception {
        // Set mocks
        when(jwtUtils.resolveToken(JWT)).thenReturn(RESOLVED_TOKEN);
        when(jwtUtils.validateAndParseClaims(RESOLVED_TOKEN)).thenReturn(claims);
        when(jwtUtils.getRole(claims)).thenReturn(LECTURER_ROLE);
        when(applicationService.checkCandidate(userId, courseId)).thenReturn(false);

        // Register listener and setup url
        HttpUrl url = mockWebServer.url(COURSES_API + COURSES_TARGET + PARAM_STARTER
            + COURSE_ID_PARAM + courseId);
        when(gatewayConfig.getPort()).thenReturn(url.port());
        when(gatewayConfig.getHost()).thenReturn(url.host());
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody(BODY_START + courseStartNextYear + BODY_END));
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(200));

        // Perform the call
        mockMvc.perform(post(HIRE_API + PARAM_STARTER + USER_ID_PARAM + userId
                + PARAM_CONTINUER + COURSE_ID_PARAM + courseId)
                .header(AUTH_BODY, JWT))
            .andExpect(status().isMethodNotAllowed())
            .andReturn();

        // Extra checks
        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        assertEquals(GET_METHOD, recordedRequest.getMethod());
        recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        assertEquals(GET_METHOD, recordedRequest.getMethod());
    }
}
