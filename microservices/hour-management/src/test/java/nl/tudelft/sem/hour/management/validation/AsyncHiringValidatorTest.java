package nl.tudelft.sem.hour.management.validation;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.util.Locale;
import nl.tudelft.sem.hour.management.config.GatewayConfig;
import nl.tudelft.sem.hour.management.dto.HourDeclarationRequest;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@AutoConfigureMockMvc
public class AsyncHiringValidatorTest {

    private static final String authorization = "Authorization";
    private static final String token = "Bearer VALIDVALID";
    private static final String content = "Content-Type";
    private static final String applicationJson = "application/json";
    private static final String get = "GET";

    private static final String contract = String.format(Locale.ROOT,
            "{\"studentId\": %d, \"courseId\": %d, \"maxHours\": %f}", 1, 1, 15.0);

    private static MockWebServer mockWebServer;

    private static GatewayConfig gatewayConfig;

    @BeforeAll
    static void setup() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        HttpUrl url = mockWebServer.url("");
        gatewayConfig = new GatewayConfig();
        gatewayConfig.setHost(url.host());
        gatewayConfig.setPort(url.port());
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void testConstructor() {
        AsyncHiringValidator validator = new AsyncHiringValidator(gatewayConfig);
        assertNotNull(validator);
    }

    @Test
    void testValidate() throws InterruptedException {
        HourDeclarationRequest declarationRequest = new HourDeclarationRequest(1, 1, 10);
        AsyncHiringValidator validator = new AsyncHiringValidator(gatewayConfig);
        HttpHeaders headers = new HttpHeaders();
        headers.add(authorization, token);

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(contract)
                .addHeader(content, applicationJson));

        // check the state
        Mono<Boolean> result = validator.validate(headers, declarationRequest.toJson());
        assertEquals(Boolean.TRUE, result.block());

        // check that request was made to correct place
        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertEquals(get, recordedRequest.getMethod());
        assertEquals("/api/hiring-service/get-contract?courseID=1", recordedRequest.getPath());
    }

    @Test
    void testValidateInvalidContract() throws InterruptedException {
        HourDeclarationRequest declarationRequest = new HourDeclarationRequest(1, 12, 10);
        AsyncHiringValidator validator = new AsyncHiringValidator(gatewayConfig);
        HttpHeaders headers = new HttpHeaders();
        headers.add(authorization, token);

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .addHeader(content, applicationJson));

        // check the state
        Mono<Boolean> result = validator.validate(headers, declarationRequest.toJson());
        assertThatExceptionOfType(ResponseStatusException.class).isThrownBy(result::block);

        // check that request was made to correct place
        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertEquals(get, recordedRequest.getMethod());
        assertEquals("/api/hiring-service/get-contract?courseID=12", recordedRequest.getPath());
    }

    @Test
    void testValidateNegativeHours() throws InterruptedException {
        HourDeclarationRequest declarationRequest = new HourDeclarationRequest(1, 1, 0);
        AsyncHiringValidator validator = new AsyncHiringValidator(gatewayConfig);
        HttpHeaders headers = new HttpHeaders();
        headers.add(authorization, token);

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(contract)
                .addHeader(content, applicationJson));

        // check the state
        Mono<Boolean> result = validator.validate(headers, declarationRequest.toJson());
        assertThatExceptionOfType(ResponseStatusException.class).isThrownBy(result::block);

        // check that request was made to correct place
        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertEquals(get, recordedRequest.getMethod());
        assertEquals("/api/hiring-service/get-contract?courseID=1", recordedRequest.getPath());
    }

    @Test
    void testValidateMoreThanMaxHours() throws InterruptedException {
        HourDeclarationRequest declarationRequest = new HourDeclarationRequest(1, 1, 999);
        AsyncHiringValidator validator = new AsyncHiringValidator(gatewayConfig);
        HttpHeaders headers = new HttpHeaders();
        headers.add(authorization, token);

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(contract)
                .addHeader(content, applicationJson));

        // check the state
        Mono<Boolean> result = validator.validate(headers, declarationRequest.toJson());
        assertThatExceptionOfType(ResponseStatusException.class).isThrownBy(result::block);

        // check that request was made to correct place
        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertEquals(get, recordedRequest.getMethod());
        assertEquals("/api/hiring-service/get-contract?courseID=1", recordedRequest.getPath());
    }

}
