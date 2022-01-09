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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@AutoConfigureMockMvc
public class AsyncCourseTimeValidatorTest {
    private static final String TOKEN = "Bearer VALIDVALID";
    private static final String COURSE_CODE = "CSE1234";

    private transient MockWebServer mockWebServer;

    private transient GatewayConfig gatewayConfig;

    @BeforeEach
    private void setupEach() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        HttpUrl url = mockWebServer.url("/");
        gatewayConfig = Mockito.mock(GatewayConfig.class);
        when(gatewayConfig.getHost()).thenReturn(url.host());
        when(gatewayConfig.getPort()).thenReturn(url.port());
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void testConstructor() {
        AsyncCourseTimeValidator validator = new AsyncCourseTimeValidator(gatewayConfig);
        assertNotNull(validator);
    }

    @Test
    void testValidate() throws InterruptedException {
        HourDeclarationRequest declarationRequest = new HourDeclarationRequest(1, 1, 10, "A");
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
        assertEquals("/api/courses/get/1", recordedRequest.getPath());
        assertEquals(TOKEN, recordedRequest.getHeader(HttpHeaders.AUTHORIZATION));
    }

    @Test
    void testValidateInvalidCourse() throws InterruptedException {
        HourDeclarationRequest declarationRequest = new HourDeclarationRequest(1, 12, 10, "B");
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
        assertEquals("/api/courses/get/12", recordedRequest.getPath());
        assertEquals(TOKEN, recordedRequest.getHeader(HttpHeaders.AUTHORIZATION));
    }

    @Test
    void testValidateBeforeStart() throws InterruptedException {
        HourDeclarationRequest declarationRequest = new HourDeclarationRequest(1, 1, 10, "C");
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
        assertEquals("/api/courses/get/1", recordedRequest.getPath());
        assertEquals(TOKEN, recordedRequest.getHeader(HttpHeaders.AUTHORIZATION));
    }

    @Test
    void testValidateAfterEnd() throws InterruptedException {
        HourDeclarationRequest declarationRequest = new HourDeclarationRequest(1, 1, 10, "D");
        AsyncCourseTimeValidator validator = new AsyncCourseTimeValidator(gatewayConfig);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, TOKEN);

        JsonObject responseBody = configureCourseResponseBody(
                ZonedDateTime.now().minusMonths(10L),
                ZonedDateTime.now().minusMonths(3L).minusDays(1L));

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
        assertEquals("/api/courses/get/1", recordedRequest.getPath());
        assertEquals(TOKEN, recordedRequest.getHeader(HttpHeaders.AUTHORIZATION));
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
