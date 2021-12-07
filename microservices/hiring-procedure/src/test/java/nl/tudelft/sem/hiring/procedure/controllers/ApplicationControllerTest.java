package nl.tudelft.sem.hiring.procedure.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import nl.tudelft.sem.hiring.procedure.services.ApplicationService;
import nl.tudelft.sem.hiring.procedure.utils.GatewayConfig;
import nl.tudelft.sem.hiring.procedure.utils.JwtSecretKey;
import nl.tudelft.sem.hiring.procedure.utils.JwtUtils;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
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

    private JwtSecretKey jwtSecretKey;

    @MockBean
    private transient JwtUtils jwtUtils;

    @SpyBean
    private transient GatewayConfig gatewayConfig;

    private static MockWebServer mockWebServer;

    private final transient long courseId = 2450;
    private final transient long userId = 521234;
    private final transient LocalDateTime courseStartNextYear = LocalDateTime.now().plusYears(1);
    private static final String resolvedToken = "yo";
    private static final String studentRole = "student";
    private static final String lecturerRole = "lecturer";
    private static final String coursesTarget = "get-start-date";
    private static final String coursesApi = "/api/courses/";
    private static final String courseIdParam = "courseId=";
    private static final String userIdParam = "userId=";
    private static final String paramStarter = "?";
    private static final String paramContinuer = "&";
    private static final String bodyStart = "{\n  \"courseStartDate\": \"";
    private static final String bodyEnd = "\"\n}";
    private static final String applyApi = "/api/hiring-procedure/apply";
    private static final String hireApi = "/api/hiring-procedure/hire-TA";
    private static final String getApplicationsApi = "/api/hiring-procedure/get-applications";
    private static final String authBody = "Authorization";
    private static final String getMethod = "GET";
    private static final String jwt = "eyJhbGciOiJIUzI1NiJ9.eyJSb2xlIjoic3R1ZGVudCIsIklz"
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
        when(jwtUtils.resolveToken(jwt)).thenReturn(resolvedToken);
        when(jwtUtils.validateToken(resolvedToken)).thenReturn(true);
        when(jwtUtils.getRole(resolvedToken)).thenReturn(studentRole);
        when(jwtUtils.getUsername(resolvedToken)).thenReturn(String.valueOf(userId));
        when(applicationService.checkDeadline(courseStartNextYear)).thenReturn(true);
        when(applicationService.checkSameApplication(userId, courseId)).thenReturn(false);

        // Register listener and setup url
        HttpUrl url = mockWebServer.url(coursesApi + coursesTarget + paramStarter
            + courseIdParam + courseId);
        gatewayConfig.setHost(url.host());
        gatewayConfig.setPort(url.port());
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody(bodyStart + courseStartNextYear + bodyEnd));

        // Perform the call
        mockMvc.perform(post(applyApi + paramStarter + courseIdParam + courseId)
                .header(authBody, jwt))
            .andExpect(status().isOk())
            .andReturn();

        // Extra checks
        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        assertEquals(getMethod, recordedRequest.getMethod());
    }

    @Test
    public void applyEndpointUserNotStudent() throws Exception {
        // Set mocks
        when(jwtUtils.resolveToken(jwt)).thenReturn(resolvedToken);
        when(jwtUtils.validateToken(resolvedToken)).thenReturn(true);
        when(jwtUtils.getRole(resolvedToken)).thenReturn(lecturerRole);

        // Perform the call
        mockMvc.perform(post(applyApi + paramStarter + courseIdParam + courseId)
                .header(authBody, jwt))
            .andExpect(status().isMethodNotAllowed())
            .andReturn();
    }

    @Test
    public void applyEndpointDeadlinePassed() throws Exception {
        // Set mocks
        when(jwtUtils.resolveToken(jwt)).thenReturn(resolvedToken);
        when(jwtUtils.validateToken(resolvedToken)).thenReturn(true);
        when(jwtUtils.getRole(resolvedToken)).thenReturn(studentRole);
        when(applicationService.checkDeadline(courseStartNextYear)).thenReturn(false);

        // Register listener and setup url
        HttpUrl url = mockWebServer.url(coursesApi + coursesTarget + paramStarter
            + courseIdParam + courseId);
        gatewayConfig.setHost(url.host());
        gatewayConfig.setPort(url.port());
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody(bodyStart + courseStartNextYear + bodyEnd));

        // Perform the call
        mockMvc.perform(post(applyApi + paramStarter + courseIdParam + courseId)
                .header(authBody, jwt))
            .andExpect(status().isMethodNotAllowed())
            .andReturn();

        // Extra checks
        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        assertEquals(getMethod, recordedRequest.getMethod());
    }

    @Test
    public void applyEndpointUserApplied() throws Exception {
        // Set mocks
        when(jwtUtils.resolveToken(jwt)).thenReturn(resolvedToken);
        when(jwtUtils.validateToken(resolvedToken)).thenReturn(true);
        when(jwtUtils.getRole(resolvedToken)).thenReturn(studentRole);
        when(jwtUtils.getUsername(resolvedToken)).thenReturn(String.valueOf(userId));
        when(applicationService.checkDeadline(courseStartNextYear)).thenReturn(true);
        when(applicationService.checkSameApplication(userId, courseId)).thenReturn(true);

        // Register listener and setup url
        HttpUrl url = mockWebServer.url(coursesApi + coursesTarget + paramStarter
            + courseIdParam + courseId);
        gatewayConfig.setHost(url.host());
        gatewayConfig.setPort(url.port());
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody(bodyStart + courseStartNextYear + bodyEnd));

        // Perform the call
        mockMvc.perform(post(applyApi + paramStarter + courseIdParam + courseId)
                .header(authBody, jwt))
            .andExpect(status().isMethodNotAllowed())
            .andReturn();

        // Extra checks
        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        assertEquals(getMethod, recordedRequest.getMethod());
    }

    @Test
    public void applyEndpointCourseNotFound() throws Exception {
        // Set mocks
        when(jwtUtils.resolveToken(jwt)).thenReturn(resolvedToken);
        when(jwtUtils.validateToken(resolvedToken)).thenReturn(true);
        when(jwtUtils.getRole(resolvedToken)).thenReturn(studentRole);

        // Register listener and setup url
        HttpUrl url = mockWebServer.url(coursesApi + coursesTarget + paramStarter
            + courseIdParam + courseId);
        gatewayConfig.setHost(url.host());
        gatewayConfig.setPort(url.port());
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(404)
            .setBody("Not found"));

        // Perform the call
        mockMvc.perform(post(applyApi + paramStarter + courseIdParam + courseId)
                .header(authBody, jwt))
            .andExpect(status().isNotFound())
            .andReturn();

        // Extra checks
        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        assertEquals(getMethod, recordedRequest.getMethod());
    }

    @Test
    public void applyEndpointInvalidToken() throws Exception {
        // Set mocks
        when(jwtUtils.resolveToken(jwt)).thenReturn(resolvedToken);
        when(jwtUtils.validateToken(resolvedToken)).thenReturn(false);

        // Perform the call
        mockMvc.perform(post(applyApi + paramStarter + courseIdParam + courseId)
                .header(authBody, jwt))
            .andExpect(status().isNotFound())
            .andReturn();
    }

    @Test
    public void getAllEndpointPass() throws Exception {
        // Set mocks
        when(jwtUtils.resolveToken(jwt)).thenReturn(resolvedToken);
        when(jwtUtils.validateToken(resolvedToken)).thenReturn(true);
        when(jwtUtils.getRole(resolvedToken)).thenReturn(lecturerRole);

        // Perform the call
        mockMvc.perform(get("/api/hiring-procedure/get-all-applications")
                .header(authBody, jwt))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void getAllEndpointNotLecturer() throws Exception {
        // Set mocks
        when(jwtUtils.resolveToken(jwt)).thenReturn(resolvedToken);
        when(jwtUtils.validateToken(resolvedToken)).thenReturn(true);
        when(jwtUtils.getRole(resolvedToken)).thenReturn(studentRole);

        // Perform the call
        mockMvc.perform(get("/api/hiring-procedure/get-all-applications")
                .header(authBody, jwt))
            .andExpect(status().isMethodNotAllowed())
            .andReturn();
    }

    @Test
    public void getAllEndpointInvalidToken() throws Exception {
        // Set mocks
        when(jwtUtils.resolveToken(jwt)).thenReturn(resolvedToken);
        when(jwtUtils.validateToken(resolvedToken)).thenReturn(false);

        // Perform the call
        mockMvc.perform(get("/api/hiring-procedure/get-all-applications")
                .header(authBody, jwt))
            .andExpect(status().isNotFound())
            .andReturn();
    }

    @Test
    public void getApplicationsEndpointPass() throws Exception {
        // Set mocks
        when(jwtUtils.resolveToken(jwt)).thenReturn(resolvedToken);
        when(jwtUtils.validateToken(resolvedToken)).thenReturn(true);
        when(jwtUtils.getRole(resolvedToken)).thenReturn(lecturerRole);

        // Register listener and setup url
        HttpUrl url = mockWebServer.url(coursesApi + coursesTarget + paramStarter
            + courseIdParam + courseId);
        gatewayConfig.setHost(url.host());
        gatewayConfig.setPort(url.port());
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody(bodyStart + courseStartNextYear + bodyEnd));

        // Perform the call
        mockMvc.perform(get(getApplicationsApi + paramStarter
                + courseIdParam + courseId)
                .header(authBody, jwt))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void getApplicationsEndpointNotLecturer() throws Exception {
        // Set mocks
        when(jwtUtils.resolveToken(jwt)).thenReturn(resolvedToken);
        when(jwtUtils.validateToken(resolvedToken)).thenReturn(true);
        when(jwtUtils.getRole(resolvedToken)).thenReturn(studentRole);

        // Perform the call
        mockMvc.perform(get(getApplicationsApi + paramStarter
                + courseIdParam + courseId)
                .header(authBody, jwt))
            .andExpect(status().isMethodNotAllowed())
            .andReturn();
    }

    @Test
    public void getApplicationsEndpointInvalidToken() throws Exception {
        // Set mocks
        when(jwtUtils.resolveToken(jwt)).thenReturn(resolvedToken);
        when(jwtUtils.validateToken(resolvedToken)).thenReturn(false);

        // Perform the call
        mockMvc.perform(get(getApplicationsApi + paramStarter
                + courseIdParam + courseId)
                .header(authBody, jwt))
            .andExpect(status().isNotFound())
            .andReturn();
    }

    @Test
    public void getApplicationsNoCourse() throws Exception {
        // Set mocks
        when(jwtUtils.resolveToken(jwt)).thenReturn(resolvedToken);
        when(jwtUtils.validateToken(resolvedToken)).thenReturn(true);
        when(jwtUtils.getRole(resolvedToken)).thenReturn(lecturerRole);

        // Register listener and setup url
        HttpUrl url = mockWebServer.url(coursesApi + coursesTarget + paramStarter
            + courseIdParam + courseId);
        gatewayConfig.setHost(url.host());
        gatewayConfig.setPort(url.port());
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(404)
            .setBody("Course not found"));

        // Perform the call
        mockMvc.perform(get(getApplicationsApi + paramStarter
                + courseIdParam + courseId)
                .header(authBody, jwt))
            .andExpect(status().isNotFound())
            .andReturn();
    }

    @Test
    public void hireEndpointTestPass() throws Exception {
        // Set mocks
        when(jwtUtils.resolveToken(jwt)).thenReturn(resolvedToken);
        when(jwtUtils.validateToken(resolvedToken)).thenReturn(true);
        when(jwtUtils.getRole(resolvedToken)).thenReturn(lecturerRole);
        when(applicationService.checkCandidate(userId, courseId)).thenReturn(true);

        // Register listener and setup url
        HttpUrl url = mockWebServer.url(coursesApi + coursesTarget + paramStarter
            + courseIdParam + courseId);
        gatewayConfig.setHost(url.host());
        gatewayConfig.setPort(url.port());
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody(bodyStart + courseStartNextYear + bodyEnd));
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(200));

        // Perform the call
        mockMvc.perform(post(hireApi + paramStarter + userIdParam + userId
                + paramContinuer + courseIdParam + courseId)
                .header(authBody, jwt))
            .andExpect(status().isOk())
            .andReturn();

        // Extra checks
        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        assertEquals(getMethod, recordedRequest.getMethod());
        recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        assertEquals(getMethod, recordedRequest.getMethod());
    }

    @Test
    public void hireEndpointInvalidToken() throws Exception {
        // Set mocks
        when(jwtUtils.resolveToken(jwt)).thenReturn(resolvedToken);
        when(jwtUtils.validateToken(resolvedToken)).thenReturn(false);

        // Perform the call
        mockMvc.perform(post(hireApi + paramStarter + userIdParam + userId
                + paramContinuer + courseIdParam + courseId)
                .header(authBody, jwt))
            .andExpect(status().isNotFound())
            .andReturn();
    }

    @Test
    public void hireEndpointNotLecturer() throws Exception {
        // Set mocks
        when(jwtUtils.resolveToken(jwt)).thenReturn(resolvedToken);
        when(jwtUtils.validateToken(resolvedToken)).thenReturn(true);
        when(jwtUtils.getRole(resolvedToken)).thenReturn(studentRole);

        // Perform the call
        mockMvc.perform(post(hireApi + paramStarter + userIdParam + userId
                + paramContinuer + courseIdParam + courseId)
                .header(authBody, jwt))
            .andExpect(status().isMethodNotAllowed())
            .andReturn();
    }

    @Test
    public void hireEndpointCourseNotFound() throws Exception {
        // Set mocks
        when(jwtUtils.resolveToken(jwt)).thenReturn(resolvedToken);
        when(jwtUtils.validateToken(resolvedToken)).thenReturn(true);
        when(jwtUtils.getRole(resolvedToken)).thenReturn(lecturerRole);

        // Register listener and setup url
        HttpUrl url = mockWebServer.url(coursesApi + coursesTarget + paramStarter
            + courseIdParam + courseId);
        gatewayConfig.setHost(url.host());
        gatewayConfig.setPort(url.port());
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(404)
            .setBody("Course not found."));

        // Perform the call
        mockMvc.perform(post(hireApi + paramStarter + userIdParam + userId
                + paramContinuer + courseIdParam + courseId)
                .header(authBody, jwt))
            .andExpect(status().isNotFound())
            .andReturn();

        // Extra checks
        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        assertEquals(getMethod, recordedRequest.getMethod());
    }

    @Test
    public void hireEndpointUserNotFound() throws Exception {
        // Set mocks
        when(jwtUtils.resolveToken(jwt)).thenReturn(resolvedToken);
        when(jwtUtils.validateToken(resolvedToken)).thenReturn(true);
        when(jwtUtils.getRole(resolvedToken)).thenReturn(lecturerRole);

        // Register listener and setup url
        HttpUrl url = mockWebServer.url(coursesApi + coursesTarget + paramStarter
            + courseIdParam + courseId);
        gatewayConfig.setHost(url.host());
        gatewayConfig.setPort(url.port());
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody(bodyStart + courseStartNextYear + bodyEnd));
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(404));

        // Perform the call
        mockMvc.perform(post(hireApi + paramStarter + userIdParam + userId
                + paramContinuer + courseIdParam + courseId)
                .header(authBody, jwt))
            .andExpect(status().isNotFound())
            .andReturn();

        // Extra checks
        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        assertEquals(getMethod, recordedRequest.getMethod());
        recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        assertEquals(getMethod, recordedRequest.getMethod());
    }

    @Test
    public void hireEndpointNotViable() throws Exception {
        // Set mocks
        when(jwtUtils.resolveToken(jwt)).thenReturn(resolvedToken);
        when(jwtUtils.validateToken(resolvedToken)).thenReturn(true);
        when(jwtUtils.getRole(resolvedToken)).thenReturn(lecturerRole);
        when(applicationService.checkCandidate(userId, courseId)).thenReturn(false);

        // Register listener and setup url
        HttpUrl url = mockWebServer.url(coursesApi + coursesTarget + paramStarter
            + courseIdParam + courseId);
        gatewayConfig.setHost(url.host());
        gatewayConfig.setPort(url.port());
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody(bodyStart + courseStartNextYear + bodyEnd));
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(200));

        // Perform the call
        mockMvc.perform(post(hireApi + paramStarter + userIdParam + userId
                + paramContinuer + courseIdParam + courseId)
                .header(authBody, jwt))
            .andExpect(status().isMethodNotAllowed())
            .andReturn();

        // Extra checks
        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        assertEquals(getMethod, recordedRequest.getMethod());
        recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        assertEquals(getMethod, recordedRequest.getMethod());
    }
}
