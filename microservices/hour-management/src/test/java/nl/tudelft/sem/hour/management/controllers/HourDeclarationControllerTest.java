package nl.tudelft.sem.hour.management.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.matchesRegex;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
import org.junit.jupiter.api.BeforeAll;
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
public class HourDeclarationControllerTest {

    private static final String declarationPath = "/api/hour-management/declaration/";

    private static final String yourDeclaration = "Your declaration with id ";

    private static final String jsonPathMessage = "$.message";

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

    private final transient Long userId = 1234L;

    private final transient HourDeclarationRequest hourDeclarationRequest =
            new HourDeclarationRequest(userId, 5678, 1, "de");
    private final transient HourDeclarationRequest hourDeclarationRequestSameStudent =
            new HourDeclarationRequest(userId, 567812, 12, "nl");
    private final transient HourDeclarationRequest hourDeclarationRequestNew =
            new HourDeclarationRequest(userId, 567812, 12, "tr");

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

        when(jwtUtils.resolveToken(Mockito.anyString())).thenReturn("");
        when(jwtUtils.validateAndParseClaims(Mockito.anyString())).thenReturn(jwsMock);
        when(jwtUtils.getRole(jwsMock)).thenReturn(AsyncRoleValidator.Roles.ADMIN.name());
        when(jwtUtils.getUserId(jwsMock)).thenReturn(userId);
    }

    @AfterEach
    void tearDown() throws IOException {
        hourDeclarationRepository.deleteAll();

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
        hourDeclarationRepository.save(hourDeclarationUnapproved);
        hourDeclarationRepository.save(hourDeclarationApproved);

        List<HourDeclaration> expectedResponseBody = hourDeclarationRepository.findAll();

        MvcResult mvcResult = mockMvc.perform(get(declarationPath)
                        .header(HttpHeaders.AUTHORIZATION, ""))
                .andReturn();

        // Wait for response
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(expectedResponseBody)));
    }

    @Test
    void testGetAllDeclarationsEmpty() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get(declarationPath)
                        .header(HttpHeaders.AUTHORIZATION, ""))
                .andReturn();

        // Wait for response
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isNotFound());
    }

    @Test
    void testPostDeclaration() throws Exception {
        JsonObject responseBody = configureCourseResponseBody(
                ZonedDateTime.now().minusWeeks(1L), ZonedDateTime.now().plusWeeks(1L));

        when(jwtUtils.getRole(Mockito.any())).thenReturn(AsyncRoleValidator.Roles.STUDENT.name());

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(responseBody.toString())
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Integer contractMaxHours = 15;
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(String.valueOf(contractMaxHours))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        MvcResult mvcResult = mockMvc.perform(post(declarationPath)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(hourDeclarationRequestNew))
                        .header(HttpHeaders.AUTHORIZATION, ""))
                .andReturn();

        // Wait for response
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(jsonPath(jsonPathMessage).exists())
                .andExpect(jsonPath(jsonPathMessage,
                        matchesRegex("Declaration with id [0-9]+ has been successfully saved.")));

        // Verify that a new declaration was created
        Long declarationId = Long.valueOf(mvcResult.getResponse().getContentAsString()
                .replaceAll("[^0-9]", ""));

        // Expected response object
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("message", "Declaration with id "
                + declarationId + " has been successfully saved.");

        Optional<HourDeclaration> saved = hourDeclarationRepository.findById(declarationId);

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
    void testPostDeclarationAdmin() throws Exception {
        JsonObject responseBody = configureCourseResponseBody(
                ZonedDateTime.now().minusWeeks(1L), ZonedDateTime.now().plusWeeks(1L));

        when(jwtUtils.getRole(Mockito.any())).thenReturn(AsyncRoleValidator.Roles.ADMIN.name());

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(responseBody.toString())
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        MvcResult mvcResult = mockMvc.perform(post(declarationPath)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(hourDeclarationRequestNew))
                        .header(HttpHeaders.AUTHORIZATION, ""))
                .andReturn();

        // Wait for response
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(jsonPath(jsonPathMessage).exists())
                .andExpect(jsonPath(jsonPathMessage,
                        matchesRegex("Declaration with id [0-9]+ has been successfully saved.")));

        // Verify that a new declaration was created
        Long declarationId = Long.valueOf(mvcResult.getResponse().getContentAsString()
                .replaceAll("[^0-9]", ""));

        // Expected response object
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("message", "Declaration with id "
                + declarationId + " has been successfully saved.");

        Optional<HourDeclaration> saved = hourDeclarationRepository.findById(declarationId);

        assertThat(saved.isEmpty()).isFalse();
        assertThat(saved.get().getStudentId()).isEqualTo(hourDeclarationRequestNew.getStudentId());
        assertThat(saved.get().getCourseId()).isEqualTo(hourDeclarationRequestNew.getCourseId());

        assertThat(mockWebServer.getRequestCount()).isEqualTo(1);
        RecordedRequest request = mockWebServer.takeRequest(10, TimeUnit.MILLISECONDS);
        assertThat(request).isNotNull();
        assertThat(request.getMethod()).isEqualTo(HttpMethod.GET.name());
    }

    @Test
    void testPostDeclarationInvalidBody() throws Exception {
        when(jwtUtils.getRole(Mockito.any())).thenReturn(AsyncRoleValidator.Roles.STUDENT.name());

        mockMvc.perform(post(declarationPath)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("")
                        .header(HttpHeaders.AUTHORIZATION, ""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testPostDeclarationInvalidCourseTime() throws Exception {
        when(jwtUtils.getRole(Mockito.any())).thenReturn(AsyncRoleValidator.Roles.STUDENT.name());

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404));

        MvcResult mvcResult = mockMvc.perform(post(declarationPath)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(hourDeclarationRequestNew))
                        .header(HttpHeaders.AUTHORIZATION, ""))
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isNotFound());
    }

    @Test
    void testPostDeclarationInvalidContract() throws Exception {
        JsonObject responseBody = configureCourseResponseBody(
                ZonedDateTime.now().minusWeeks(1L), ZonedDateTime.now().plusWeeks(1L));

        when(jwtUtils.getRole(Mockito.any())).thenReturn(AsyncRoleValidator.Roles.STUDENT.name());

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(responseBody.toString())
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404));

        MvcResult mvcResult = mockMvc.perform(post(declarationPath)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(hourDeclarationRequestNew))
                        .header(HttpHeaders.AUTHORIZATION, ""))
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetSpecifiedDeclaration() throws Exception {
        HourDeclaration saved = hourDeclarationRepository.save(hourDeclarationUnapproved);

        Optional<HourDeclaration> expectedResponseBody = hourDeclarationRepository
                .findById(saved.getDeclarationId());

        MvcResult mvcResult = mockMvc.perform(get(declarationPath
                        + saved.getDeclarationId())
                        .header(HttpHeaders.AUTHORIZATION, ""))
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
                        .header(HttpHeaders.AUTHORIZATION, ""))
                .andReturn();

        // Wait for response
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetSpecifiedDeclarationUserAccessesTheirOwnDeclarations() throws Exception {
        HourDeclaration saved = hourDeclarationRepository.save(hourDeclarationUnapproved);

        Optional<HourDeclaration> expectedResponseBody = hourDeclarationRepository
                .findById(saved.getDeclarationId());

        when(jwtUtils.getRole(Mockito.any())).thenReturn(AsyncRoleValidator.Roles.STUDENT.name());
        when(jwtUtils.getUserId(jwsMock)).thenReturn(1234L);

        MvcResult mvcResult = mockMvc.perform(get(declarationPath
                        + saved.getDeclarationId())
                        .header(HttpHeaders.AUTHORIZATION, ""))
                .andReturn();

        // Wait for response
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(expectedResponseBody)));
    }

    @Test
    void testRejectDeclaration() throws Exception {
        HourDeclaration saved = hourDeclarationRepository.save(hourDeclarationUnapproved);

        when(jwtUtils.getRole(Mockito.any())).thenReturn(AsyncRoleValidator.Roles.LECTURER.name());
        when(jwtUtils.getUserId(jwsMock)).thenReturn(saved.getDeclarationId());

        // Enqueue response that tells us that the user teaches the course
        // associated to the declaration ID
        mockWebServer.enqueue(new MockResponse().setResponseCode(HttpStatus.OK.value()));

        when(notificationService.notify(1234L,
                yourDeclaration + saved.getDeclarationId() + " has been rejected.",
                ""))
                .thenReturn(Mono.empty());

        MvcResult mvcResult = mockMvc.perform(delete(declarationPath
                        + saved.getDeclarationId() + "/reject")
                        .header(HttpHeaders.AUTHORIZATION, ""))
                .andReturn();

        // Wait for response
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk());

        Optional<HourDeclaration> hourDeclaration = hourDeclarationRepository
                .findById(saved.getDeclarationId());

        // ensures that delete is no longer in system
        assertThat(hourDeclaration.isEmpty()).isTrue();

        // Verify that a request was sent to the course microservice
        RecordedRequest request = mockWebServer.takeRequest(10, TimeUnit.MILLISECONDS);
        assertThat(request).isNotNull();
    }

    @Test
    void testRejectDeclarationInvalidId() throws Exception {
        when(jwtUtils.getRole(Mockito.any())).thenReturn(AsyncRoleValidator.Roles.LECTURER.name());
        MvcResult mvcResult = mockMvc.perform(delete("/api/hour-management/declaration/9999/reject")
                        .header(HttpHeaders.AUTHORIZATION, ""))
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isNotFound());
    }

    @Test
    void testRejectDeclarationNotificationFail() throws Exception {
        HourDeclaration saved = hourDeclarationRepository.save(hourDeclarationUnapproved);

        when(jwtUtils.getRole(Mockito.any())).thenReturn(AsyncRoleValidator.Roles.LECTURER.name());
        when(jwtUtils.getUserId(jwsMock)).thenReturn(saved.getDeclarationId());

        // Enqueue response that tells us that the user teaches the course
        // associated to the declaration ID
        mockWebServer.enqueue(new MockResponse().setResponseCode(HttpStatus.OK.value()));

        when(notificationService.notify(1234L,
                yourDeclaration + saved.getDeclarationId() + " has been rejected.",
                ""))
                .thenReturn(Mono.error(new ResponseStatusException(HttpStatus.CONFLICT,
                        "Failed to register notification")));

        MvcResult mvcResult = mockMvc.perform(delete(declarationPath
                        + saved.getDeclarationId() + "/reject")
                        .header(HttpHeaders.AUTHORIZATION, ""))
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isConflict());

        // Verify that a request was sent to the course microservice
        RecordedRequest request = mockWebServer.takeRequest(10, TimeUnit.MILLISECONDS);
        assertThat(request).isNotNull();
    }

    @Test
    void testRejectDeclarationAlreadyApproved() throws Exception {
        HourDeclaration saved = hourDeclarationRepository.save(hourDeclarationApproved);

        when(jwtUtils.getRole(Mockito.any())).thenReturn(AsyncRoleValidator.Roles.LECTURER.name());
        when(jwtUtils.getUserId(jwsMock)).thenReturn(saved.getDeclarationId());

        // Enqueue response that tells us that the user teaches the course
        // associated to the declaration ID
        mockWebServer.enqueue(new MockResponse().setResponseCode(HttpStatus.OK.value()));

        MvcResult mvcResult = mockMvc.perform(delete(declarationPath
                        + saved.getDeclarationId() + "/reject")
                        .header(HttpHeaders.AUTHORIZATION, ""))
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isBadRequest());

        // Verify that a request was sent to the course microservice
        RecordedRequest request = mockWebServer.takeRequest(10, TimeUnit.MILLISECONDS);
        assertThat(request).isNotNull();
    }

    @Test
    void testApproveDeclaration() throws Exception {
        HourDeclaration saved = hourDeclarationRepository.save(hourDeclarationUnapproved);

        when(jwtUtils.getRole(Mockito.any())).thenReturn(AsyncRoleValidator.Roles.LECTURER.name());
        when(jwtUtils.getUserId(jwsMock)).thenReturn(saved.getDeclarationId());

        // Enqueue response that tells us that the user teaches the course
        // associated to the declaration ID
        mockWebServer.enqueue(new MockResponse().setResponseCode(HttpStatus.OK.value()));

        when(notificationService.notify(1234L,
                yourDeclaration + saved.getDeclarationId() +  " has been approved.",
                ""))
                .thenReturn(Mono.empty());

        MvcResult mvcResult = mockMvc.perform(put(declarationPath
                        + saved.getDeclarationId() + "/approve")
                        .contentType("application/json")
                        .header(HttpHeaders.AUTHORIZATION, ""))
                .andReturn();

        // Wait for response
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk());

        Optional<HourDeclaration> hourDeclaration = hourDeclarationRepository
                .findById(saved.getDeclarationId());

        assertThat(hourDeclaration.isEmpty()).isFalse();
        assertThat(hourDeclaration.get().isApproved()).isTrue();

        // Verify that a request was sent to the course microservice
        RecordedRequest request = mockWebServer.takeRequest(10, TimeUnit.MILLISECONDS);
        assertThat(request).isNotNull();
    }

    @Test
    void testApproveDeclarationInvalidId() throws Exception {
        when(jwtUtils.getRole(Mockito.any())).thenReturn(AsyncRoleValidator.Roles.LECTURER.name());

        MvcResult mvcResult = mockMvc.perform(put("/api/hour-management/declaration/9999/approve")
                        .header(HttpHeaders.AUTHORIZATION, ""))
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isNotFound());
    }


    @Test
    void testApproveDeclarationNotificationFail() throws Exception {
        HourDeclaration saved = hourDeclarationRepository.save(hourDeclarationUnapproved);

        when(jwtUtils.getRole(Mockito.any())).thenReturn(AsyncRoleValidator.Roles.LECTURER.name());
        when(jwtUtils.getUserId(jwsMock)).thenReturn(saved.getStudentId());

        // Enqueue response that tells us that the user teaches the course
        // associated to the declaration ID
        mockWebServer.enqueue(new MockResponse().setResponseCode(HttpStatus.OK.value()));

        when(notificationService.notify(1234L,
                yourDeclaration + saved.getDeclarationId() + " has been approved.",
                ""))
                .thenReturn(Mono.error(new ResponseStatusException(HttpStatus.CONFLICT,
                        "Failed to register notification")));

        MvcResult mvcResult = mockMvc.perform(put(declarationPath
                        + saved.getDeclarationId() + "/approve")
                        .header(HttpHeaders.AUTHORIZATION, ""))
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isConflict());


        // Verify that a request was sent to the course microservice
        RecordedRequest request = mockWebServer.takeRequest(10, TimeUnit.MILLISECONDS);
        assertThat(request).isNotNull();
    }

    @Test
    void testApproveDeclarationAlreadyApproved() throws Exception {
        HourDeclaration saved = hourDeclarationRepository.save(hourDeclarationApproved);

        when(jwtUtils.getRole(Mockito.any())).thenReturn(AsyncRoleValidator.Roles.LECTURER.name());
        when(jwtUtils.getUserId(jwsMock)).thenReturn(saved.getDeclarationId());

        // Enqueue response that tells us that the user teaches the course
        // associated to the declaration ID
        mockWebServer.enqueue(new MockResponse().setResponseCode(HttpStatus.OK.value()));

        MvcResult mvcResult = mockMvc.perform(put(declarationPath
                        + saved.getDeclarationId() + "/approve")
                        .header(HttpHeaders.AUTHORIZATION, ""))
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isBadRequest());


        // Verify that a request was sent to the course microservice
        RecordedRequest request = mockWebServer.takeRequest(10, TimeUnit.MILLISECONDS);
        assertThat(request).isNotNull();
    }

    @Test
    void getAllUnapproved() throws Exception {
        hourDeclarationRepository.save(hourDeclarationUnapproved);
        hourDeclarationRepository.save(hourDeclarationApproved);

        List<HourDeclaration> expectedResponseBody = hourDeclarationRepository
                .findByApproved(false);

        MvcResult mvcResult = mockMvc.perform(get("/api/hour-management/declaration/unapproved")
                        .header(HttpHeaders.AUTHORIZATION, ""))
                .andReturn();

        // Wait for response
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(expectedResponseBody)));
    }

    @Test
    void testGetAllUnapprovedDeclarationsEmpty() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/api/hour-management/declaration/unapproved")
                        .header(HttpHeaders.AUTHORIZATION, ""))
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllUnapprovedForCourse() throws Exception {
        // Create test declarations
        long courseId = 1337;
        Set<HourDeclaration> declarations = Stream.of(
                new HourDeclarationRequest(1234, courseId, 10.0, "de"),
                new HourDeclarationRequest(1235, courseId, 5.0, "nl"),
                new HourDeclarationRequest(1234, courseId, 16.5, "tr")
        ).map(HourDeclaration::new).collect(Collectors.toSet());

        // Store all test declarations
        hourDeclarationRepository.saveAll(declarations);

        // Perform the request for the specified course
        MvcResult mvcResult = mockMvc.perform(get("/api/hour-management/declaration/unapproved/"
                        + courseId)
                        .header(HttpHeaders.AUTHORIZATION, ""))
                .andReturn();

        // Determine the expected result
        List<HourDeclaration> expectedResponseBody = hourDeclarationRepository
                .findByCourseIdAndApproved(courseId, false);

        // Wait for response and check if it matches the result
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(expectedResponseBody)));
    }

    @Test
    void testGetAllUnapprovedDeclarationsForCourseEmpty() throws Exception {
        // Course for which no declaration exists
        long courseId = 1337;
        hourDeclarationRepository.deleteAll();

        // Perform the request for the specified course
        MvcResult mvcResult = mockMvc.perform(get("/api/hour-management/declaration/unapproved/"
                + courseId)
                        .header(HttpHeaders.AUTHORIZATION, ""))
                .andReturn();

        // Verify that there are no declarations for the specified course -> bad request
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetAllDeclarationsByStudent() throws Exception {
        hourDeclarationRepository.save(hourDeclarationUnapproved);
        hourDeclarationRepository.save(hourDeclarationApproved);
        HourDeclaration saved = hourDeclarationRepository.save(hourDeclarationSameStudent);

        List<HourDeclaration> expectedResponseBody = hourDeclarationRepository
                .findByStudentId(saved.getStudentId());

        MvcResult mvcResult = mockMvc.perform(get("/api/hour-management/declaration/student/"
                        + saved.getStudentId())
                        .header(HttpHeaders.AUTHORIZATION, ""))
                .andReturn();

        // Wait for response
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(expectedResponseBody)));
    }

    @Test
    void testGetAllDeclarationsByStudentEmpty() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/api/hour-management/declaration/student/9999")
                        .header(HttpHeaders.AUTHORIZATION, ""))
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetAllDeclarationsByStudentUserAccessesTheirOwnDeclarations() throws Exception {
        hourDeclarationRepository.save(hourDeclarationUnapproved);
        hourDeclarationRepository.save(hourDeclarationApproved);
        HourDeclaration saved = hourDeclarationRepository.save(hourDeclarationSameStudent);

        List<HourDeclaration> expectedResponseBody = hourDeclarationRepository
                .findByStudentId(saved.getStudentId());

        when(jwtUtils.getRole(Mockito.any())).thenReturn(AsyncRoleValidator.Roles.STUDENT.name());
        when(jwtUtils.getUserId(jwsMock)).thenReturn(saved.getStudentId());

        MvcResult mvcResult = mockMvc.perform(get("/api/hour-management/declaration/student/"
                        + saved.getStudentId())
                        .header(HttpHeaders.AUTHORIZATION, ""))
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
