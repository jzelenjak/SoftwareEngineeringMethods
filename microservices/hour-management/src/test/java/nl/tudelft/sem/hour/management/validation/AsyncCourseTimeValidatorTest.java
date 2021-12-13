package nl.tudelft.sem.hour.management.validation;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.time.ZonedDateTime;
import nl.tudelft.sem.hour.management.config.GatewayConfig;
import nl.tudelft.sem.hour.management.dto.HourDeclarationRequest;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@AutoConfigureMockMvc
@SpringBootTest(classes = {GatewayConfig.class, AsyncCourseTimeValidator.class})
public class AsyncCourseTimeValidatorTest {
    private static final String TOKEN = "Bearer VALIDVALID";
    private static final String COURSE_CODE = "CSE1234";

    private static MockWebServer mockWebServer;

    @MockBean
    private transient GatewayConfig gatewayConfig;

    @BeforeAll
    static void setup() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @BeforeEach
    private void setupEach() {
        HttpUrl url = mockWebServer.url("/");
        when(gatewayConfig.getHost()).thenReturn(url.host());
        when(gatewayConfig.getPort()).thenReturn(url.port());
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void testConstructor() {
        AsyncCourseTimeValidator validator = new AsyncCourseTimeValidator(gatewayConfig);
        assertNotNull(validator);
    }

    @Test
    void testValidate() throws InterruptedException {
        HourDeclarationRequest declarationRequest = new HourDeclarationRequest(1, 1, 10);
        AsyncCourseTimeValidator validator = new AsyncCourseTimeValidator(gatewayConfig);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, TOKEN);

        JsonObject responseBody = configureCourseResponseBody(
                ZonedDateTime.now().minusWeeks(1L), ZonedDateTime.now().plusWeeks(1L));

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(responseBody.toString())
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // check the state
        Mono<Boolean> result = validator.validate(headers, declarationRequest.toJson());
        assertEquals(Boolean.TRUE, result.block());

        // check that request was made to correct place
        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertEquals(HttpMethod.GET.name(), recordedRequest.getMethod());
        assertEquals("/api/courses/info?courseID=1", recordedRequest.getPath());
    }

    @Test
    void testValidateInvalidCourse() throws InterruptedException {
        HourDeclarationRequest declarationRequest = new HourDeclarationRequest(1, 12, 10);
        AsyncCourseTimeValidator validator = new AsyncCourseTimeValidator(gatewayConfig);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, TOKEN);

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // check the state
        Mono<Boolean> result = validator.validate(headers, declarationRequest.toJson());
        assertThatExceptionOfType(ResponseStatusException.class).isThrownBy(result::block);

        // check that request was made to correct place
        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertEquals(HttpMethod.GET.name(), recordedRequest.getMethod());
        assertEquals("/api/courses/info?courseID=12", recordedRequest.getPath());
    }

    @Test
    void testValidateBeforeStart() throws InterruptedException {
        HourDeclarationRequest declarationRequest = new HourDeclarationRequest(1, 1, 10);
        AsyncCourseTimeValidator validator = new AsyncCourseTimeValidator(gatewayConfig);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, TOKEN);

        JsonObject responseBody = configureCourseResponseBody(
                ZonedDateTime.now().plusDays(1L), ZonedDateTime.now().plusWeeks(10L));

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(responseBody.toString())
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // check the state
        Mono<Boolean> result = validator.validate(headers, declarationRequest.toJson());
        assertThatExceptionOfType(ResponseStatusException.class).isThrownBy(result::block);

        // check that request was made to correct place
        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertEquals(HttpMethod.GET.name(), recordedRequest.getMethod());
        assertEquals("/api/courses/info?courseID=1", recordedRequest.getPath());
    }

    @Test
    void testValidateAfterEnd() throws InterruptedException {
        HourDeclarationRequest declarationRequest = new HourDeclarationRequest(1, 1, 10);
        AsyncCourseTimeValidator validator = new AsyncCourseTimeValidator(gatewayConfig);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, TOKEN);

        JsonObject responseBody = configureCourseResponseBody(
                ZonedDateTime.now().minusWeeks(10L),
                ZonedDateTime.now().minusWeeks(3L).minusDays(1L));

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(responseBody.toString())
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // check the state
        Mono<Boolean> result = validator.validate(headers, declarationRequest.toJson());
        assertThatExceptionOfType(ResponseStatusException.class).isThrownBy(result::block);

        // check that request was made to correct place
        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertEquals(HttpMethod.GET.name(), recordedRequest.getMethod());
        assertEquals("/api/courses/info?courseID=1", recordedRequest.getPath());
    }

    private JsonObject configureCourseResponseBody(ZonedDateTime start, ZonedDateTime end) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("courseId", 1L);
        jsonObject.addProperty("courseCode", AsyncCourseTimeValidatorTest.COURSE_CODE);
        jsonObject.addProperty("startDate", start.toString());
        jsonObject.addProperty("endDate", end.toString());

        return jsonObject;
    }

}
