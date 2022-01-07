package nl.tudelft.sem.hour.management.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import nl.tudelft.sem.hour.management.config.GatewayConfig;
import nl.tudelft.sem.hour.management.dto.HourDeclarationRequest;
import nl.tudelft.sem.hour.management.entities.HourDeclaration;
import nl.tudelft.sem.hour.management.repositories.HourDeclarationRepository;
import nl.tudelft.sem.hour.management.services.NotificationService;
import nl.tudelft.sem.hour.management.validation.AsyncRoleValidator;
import nl.tudelft.sem.jwt.JwtUtils;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class HourDeclarationControllerTest {

    private static final String declarationPath = "/api/hour-management/declaration";

    private static final String authorization = "Authorization";

    @Autowired
    private transient MockMvc mockMvc;

    @Autowired
    private transient ObjectMapper objectMapper;

    @Autowired
    private transient HourDeclarationRepository hourDeclarationRepository;

    @MockBean
    private transient NotificationService notificationService;

    @MockBean
    private transient JwtUtils jwtUtils;

    private transient MockWebServer mockWebServer;

    @MockBean
    private transient GatewayConfig gatewayConfig;

    @Mock
    private transient Jws<Claims> jwsMock;

    private final transient ZonedDateTime testDate = ZonedDateTime.now();

    private final transient HourDeclarationRequest hourDeclarationRequest =
            new HourDeclarationRequest(1234, 5678, 1);
    private final transient HourDeclarationRequest hourDeclarationRequestSameStudent =
            new HourDeclarationRequest(1234, 567812, 12);
    private final transient HourDeclarationRequest hourDeclarationRequestNew =
            new HourDeclarationRequest(12345, 567812, 12);

    private final transient HourDeclaration hourDeclarationUnapproved = new HourDeclaration(1,
            hourDeclarationRequest, false, testDate);
    private final transient HourDeclaration hourDeclarationApproved = new HourDeclaration(2,
            hourDeclarationRequestNew, true, testDate);
    private final transient HourDeclaration hourDeclarationSameStudent = new HourDeclaration(3,
            hourDeclarationRequestSameStudent, false, testDate);

    @BeforeEach
    void init() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        HttpUrl url = mockWebServer.url("");
        when(gatewayConfig.getHost()).thenReturn(url.host());
        when(gatewayConfig.getPort()).thenReturn(url.port());

        hourDeclarationRepository.deleteAll();

        hourDeclarationRepository.save(hourDeclarationUnapproved);
        hourDeclarationRepository.save(hourDeclarationApproved);

        when(jwtUtils.resolveToken(Mockito.any())).thenReturn("");
        when(jwtUtils.validateAndParseClaims(Mockito.any())).thenReturn(jwsMock);
        when(jwtUtils.getRole(Mockito.any())).thenReturn(AsyncRoleValidator.Roles.ADMIN.name());
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void testGreeting() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/api/hour-management"))
                .andExpect(status().isOk())
                .andReturn();

        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat("Hello from Hour Management").isEqualTo(actualResponseBody);
    }

    @Test
    void testGetAllDeclarations() throws Exception {
        List<HourDeclaration> expectedResponseBody = hourDeclarationRepository.findAll();

        MvcResult mvcResult = mockMvc.perform(get(declarationPath)
                        .header(authorization, ""))
                .andReturn();

        // Wait for response
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(expectedResponseBody)));
    }

    @Test
    void testGetAllDeclarationsEmpty() throws Exception {
        hourDeclarationRepository.deleteAll();

        MvcResult mvcResult = mockMvc.perform(get(declarationPath)
                        .header(authorization, ""))
                .andReturn();

        // Wait for response
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isNotFound());
    }

    @Test
    void testPostDeclaration() throws Exception {
        JsonObject responseBody = configureCourseResponseBody(
                ZonedDateTime.now().minusWeeks(1L), ZonedDateTime.now().plusWeeks(1L));

        String contract = String.format(Locale.ROOT,
                "{\"studentId\": %d, \"courseId\": %d, \"maxHours\": %f}", 1, 1, 15.0);

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(responseBody.toString())
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(contract)
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        MvcResult mvcResult = mockMvc.perform(post(declarationPath)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(hourDeclarationRequestNew))
                        .header(authorization, ""))
                .andReturn();

        // Expected response object
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("message", "Declaration with id 3 has been successfully saved.");

        // Wait for response
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content().json(jsonObject.toString()));

        Optional<HourDeclaration> saved = hourDeclarationRepository.findById(3L);

        assertThat(saved.isEmpty()).isFalse();
        assertThat(saved.get().getStudentId()).isEqualTo(hourDeclarationRequestNew.getStudentId());
        assertThat(saved.get().getCourseId()).isEqualTo(hourDeclarationRequestNew.getCourseId());

        RecordedRequest request = mockWebServer.takeRequest(10, TimeUnit.MILLISECONDS);
        assertThat(request).isNotNull();
        assertThat(request.getMethod()).isEqualTo(HttpMethod.GET.name());

        request = mockWebServer.takeRequest(10, TimeUnit.MILLISECONDS);
        assertThat(request).isNotNull();
        assertThat(request.getMethod()).isEqualTo(HttpMethod.GET.name());
    }

    @Test
    void testPostDeclarationInvalid() throws Exception {
        mockMvc.perform(post(declarationPath)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("")
                        .header(authorization, ""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testPostDeclarationInvalidCourseTime() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404));

        MvcResult mvcResult = mockMvc.perform(post(declarationPath)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(hourDeclarationRequestNew))
                        .header(authorization, ""))
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isNotFound());
    }

    @Test
    void testPostDeclarationInvalidContract() throws Exception {
        JsonObject responseBody = configureCourseResponseBody(
                ZonedDateTime.now().minusWeeks(1L), ZonedDateTime.now().plusWeeks(1L));

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(responseBody.toString())
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404));

        MvcResult mvcResult = mockMvc.perform(post(declarationPath)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(hourDeclarationRequestNew))
                        .header(authorization, ""))
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetSpecifiedDeclaration() throws Exception {
        Optional<HourDeclaration> expectedResponseBody = hourDeclarationRepository.findById(1L);

        MvcResult mvcResult = mockMvc.perform(get("/api/hour-management/declaration/1")
                        .header(authorization, ""))
                .andExpect(status().isOk())
                .andReturn();

        // Wait for response
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(expectedResponseBody)));
    }

    @Test
    void testGetSpecifiedDeclarationInvalidId() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/api/hour-management/declaration/9999")
                        .header(authorization, ""))
                .andReturn();

        // Wait for response
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetSpecifiedDeclarationUserAccessesTheirOwnDeclarations() throws Exception {
        Optional<HourDeclaration> expectedResponseBody = hourDeclarationRepository.findById(1L);

        when(jwtUtils.getRole(Mockito.any())).thenReturn(AsyncRoleValidator.Roles.STUDENT.name());
        when(jwtUtils.getUserId(jwsMock)).thenReturn(1234L);

        MvcResult mvcResult = mockMvc.perform(get("/api/hour-management/declaration/1")
                        .header(authorization, ""))
                .andReturn();

        // Wait for response
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(expectedResponseBody)));
    }

    @Test
    void testRejectDeclaration() throws Exception {
        when(jwtUtils.getRole(Mockito.any())).thenReturn(AsyncRoleValidator.Roles.LECTURER.name());
        when(jwtUtils.getUserId(jwsMock)).thenReturn(1L);

        // Enqueue response that tells us that the user teaches the course
        // associated to the declaration ID
        mockWebServer.enqueue(new MockResponse().setResponseCode(HttpStatus.OK.value()));

        when(notificationService.notify(1234L,
                "Your declaration with id 1 has been rejected.",
                ""))
                .thenReturn(Mono.empty());

        MvcResult mvcResult = mockMvc.perform(delete("/api/hour-management/declaration/1/reject")
                        .header(authorization, ""))
                .andReturn();

        // Wait for response
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk());

        Optional<HourDeclaration> hourDeclaration = hourDeclarationRepository.findById(1L);

        // ensures that delete is no longer in system
        assertThat(hourDeclaration.isEmpty()).isTrue();

        // Verify that a request was sent to the course microservice
        RecordedRequest request = mockWebServer.takeRequest(10, TimeUnit.MILLISECONDS);
        assertThat(request).isNotNull();
    }

    @Test
    void testRejectDeclarationInvalidId() throws Exception {
        when(jwtUtils.getRole(Mockito.any())).thenReturn(AsyncRoleValidator.Roles.LECTURER.name());
        MvcResult mvcResult = mockMvc.perform(delete("/api/hour-management/declaration/20/reject")
                        .header(authorization, ""))
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isNotFound());
    }

    @Test
    void testRejectDeclarationNotificationFail() throws Exception {
        when(jwtUtils.getRole(Mockito.any())).thenReturn(AsyncRoleValidator.Roles.LECTURER.name());
        when(jwtUtils.getUserId(jwsMock)).thenReturn(1L);

        // Enqueue response that tells us that the user teaches the course
        // associated to the declaration ID
        mockWebServer.enqueue(new MockResponse().setResponseCode(HttpStatus.OK.value()));

        when(notificationService.notify(1234L,
                "Your declaration with id 1 has been rejected.",
                ""))
                .thenReturn(Mono.error(new ResponseStatusException(HttpStatus.CONFLICT,
                        "Failed to register notification")));

        MvcResult mvcResult = mockMvc.perform(delete("/api/hour-management/declaration/1/reject")
                        .header(authorization, ""))
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isConflict());

        // Verify that a request was sent to the course microservice
        RecordedRequest request = mockWebServer.takeRequest(10, TimeUnit.MILLISECONDS);
        assertThat(request).isNotNull();
    }

    @Test
    void testRejectDeclarationAlreadyApproved() throws Exception {
        when(jwtUtils.getRole(Mockito.any())).thenReturn(AsyncRoleValidator.Roles.LECTURER.name());
        when(jwtUtils.getUserId(jwsMock)).thenReturn(1L);

        // Enqueue response that tells us that the user teaches the course
        // associated to the declaration ID
        mockWebServer.enqueue(new MockResponse().setResponseCode(HttpStatus.OK.value()));

        MvcResult mvcResult = mockMvc.perform(delete("/api/hour-management/declaration/2/reject")
                        .header(authorization, ""))
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isBadRequest());

        // Verify that a request was sent to the course microservice
        RecordedRequest request = mockWebServer.takeRequest(10, TimeUnit.MILLISECONDS);
        assertThat(request).isNotNull();
    }

    @Test
    void testApproveDeclaration() throws Exception {
        when(jwtUtils.getRole(Mockito.any())).thenReturn(AsyncRoleValidator.Roles.LECTURER.name());
        when(jwtUtils.getUserId(jwsMock)).thenReturn(1L);

        // Enqueue response that tells us that the user teaches the course
        // associated to the declaration ID
        mockWebServer.enqueue(new MockResponse().setResponseCode(HttpStatus.OK.value()));

        when(notificationService.notify(1234L,
                "Your declaration with id 1 has been approved.",
                ""))
                .thenReturn(Mono.empty());

        MvcResult mvcResult = mockMvc.perform(put("/api/hour-management/declaration/1/approve")
                        .contentType("application/json")
                        .header(authorization, ""))
                .andReturn();

        // Wait for response
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk());

        Optional<HourDeclaration> hourDeclaration = hourDeclarationRepository.findById(1L);

        assertThat(hourDeclaration.isEmpty()).isFalse();
        assertThat(hourDeclaration.get().isApproved()).isTrue();

        // Verify that a request was sent to the course microservice
        RecordedRequest request = mockWebServer.takeRequest(10, TimeUnit.MILLISECONDS);
        assertThat(request).isNotNull();
    }

    @Test
    void testApproveDeclarationInvalidId() throws Exception {
        when(jwtUtils.getRole(Mockito.any())).thenReturn(AsyncRoleValidator.Roles.LECTURER.name());
        MvcResult mvcResult = mockMvc.perform(put("/api/hour-management/declaration/20/approve")
                        .header(authorization, ""))
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isNotFound());
    }


    @Test
    void testApproveDeclarationNotificationFail() throws Exception {
        when(jwtUtils.getRole(Mockito.any())).thenReturn(AsyncRoleValidator.Roles.LECTURER.name());
        when(jwtUtils.getUserId(jwsMock)).thenReturn(1L);

        // Enqueue response that tells us that the user teaches the course
        // associated to the declaration ID
        mockWebServer.enqueue(new MockResponse().setResponseCode(HttpStatus.OK.value()));

        when(notificationService.notify(1234L,
                "Your declaration with id 1 has been approved.",
                ""))
                .thenReturn(Mono.error(new ResponseStatusException(HttpStatus.CONFLICT,
                        "Failed to register notification")));

        MvcResult mvcResult = mockMvc.perform(put("/api/hour-management/declaration/1/approve")
                        .header(authorization, ""))
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isConflict());


        // Verify that a request was sent to the course microservice
        RecordedRequest request = mockWebServer.takeRequest(10, TimeUnit.MILLISECONDS);
        assertThat(request).isNotNull();
    }

    @Test
    void testApproveDeclarationAlreadyApproved() throws Exception {
        when(jwtUtils.getRole(Mockito.any())).thenReturn(AsyncRoleValidator.Roles.LECTURER.name());
        when(jwtUtils.getUserId(jwsMock)).thenReturn(1L);

        // Enqueue response that tells us that the user teaches the course
        // associated to the declaration ID
        mockWebServer.enqueue(new MockResponse().setResponseCode(HttpStatus.OK.value()));

        MvcResult mvcResult = mockMvc.perform(put("/api/hour-management/declaration/2/approve")
                        .header(authorization, ""))
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isBadRequest());


        // Verify that a request was sent to the course microservice
        RecordedRequest request = mockWebServer.takeRequest(10, TimeUnit.MILLISECONDS);
        assertThat(request).isNotNull();
    }

    @Test
    void getAllUnapproved() throws Exception {
        List<HourDeclaration> expectedResponseBody = hourDeclarationRepository
                .findByApproved(false);

        MvcResult mvcResult = mockMvc.perform(get("/api/hour-management/declaration/unapproved")
                        .header(authorization, ""))
                .andReturn();

        // Wait for response
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(expectedResponseBody)));
    }

    @Test
    void testGetAllUnapprovedDeclarationsEmpty() throws Exception {
        hourDeclarationRepository.deleteAll();

        MvcResult mvcResult = mockMvc.perform(get("/api/hour-management/declaration/unapproved")
                        .header(authorization, ""))
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetAllDeclarationsByStudent() throws Exception {
        List<HourDeclaration> expectedResponseBody = hourDeclarationRepository
                .findByStudentId(1234);

        MvcResult mvcResult = mockMvc.perform(get("/api/hour-management/declaration/student/1234")
                        .header(authorization, ""))
                .andReturn();

        // Wait for response
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(expectedResponseBody)));
    }

    @Test
    void testGetAllDeclarationsByStudentEmpty() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/api/hour-management/declaration/student/9999")
                        .header(authorization, ""))
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetAllDeclarationsByStudentUserAccessesTheirOwnDeclarations() throws Exception {
        List<HourDeclaration> expectedResponseBody = hourDeclarationRepository
                .findByStudentId(1234);

        when(jwtUtils.getRole(Mockito.any())).thenReturn(AsyncRoleValidator.Roles.STUDENT.name());
        when(jwtUtils.getUserId(jwsMock)).thenReturn(1234L);

        MvcResult mvcResult = mockMvc.perform(get("/api/hour-management/declaration/student/1234")
                        .header(authorization, ""))
                .andReturn();

        // Wait for response
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(expectedResponseBody)));
    }

    private JsonObject configureCourseResponseBody(ZonedDateTime start, ZonedDateTime end) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("courseId", 1L);
        jsonObject.addProperty("courseCode", "CSE1234");
        jsonObject.addProperty("startDate", start.toString());
        jsonObject.addProperty("endDate", end.toString());

        return jsonObject;
    }
}
