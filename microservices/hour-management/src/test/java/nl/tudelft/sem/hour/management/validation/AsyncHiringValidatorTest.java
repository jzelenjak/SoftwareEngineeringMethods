package nl.tudelft.sem.hour.management.validation;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import java.io.IOException;
import java.util.Locale;
import nl.tudelft.sem.hour.management.config.GatewayConfig;
import nl.tudelft.sem.hour.management.dto.HourDeclarationRequest;
import nl.tudelft.sem.jwt.JwtUtils;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
public class AsyncHiringValidatorTest {

    private static final String token = "Bearer VALIDVALID";
    private static final String content = "Content-Type";
    private static final String applicationJson = "application/json";
    private static final String get = "GET";

    private static final String contract = String.format(Locale.ROOT,
            "{\"studentId\": %d, \"courseId\": %d, \"maxHours\": %f}", 1, 1, 15.0);

    private transient MockWebServer mockWebServer;

    private transient GatewayConfig gatewayConfig;

    private transient HttpHeaders headers;

    @MockBean
    private transient JwtUtils jwtUtils;

    @Mock
    private transient Jws<Claims> jwsMock;

    @BeforeEach
    void setupEach() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        HttpUrl url = mockWebServer.url("");
        gatewayConfig = Mockito.mock(GatewayConfig.class);
        when(gatewayConfig.getHost()).thenReturn(url.host());
        when(gatewayConfig.getPort()).thenReturn(url.port());

        when(jwtUtils.resolveToken(token)).thenReturn("");
        when(jwtUtils.validateAndParseClaims(anyString())).thenReturn(jwsMock);
        when(jwtUtils.getRole(jwsMock)).thenReturn(AsyncRoleValidator.Roles.STUDENT.name());

        headers = Mockito.mock(HttpHeaders.class);
        when(headers.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn(token);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void testConstructor() {
        AsyncHiringValidator validator = new AsyncHiringValidator(gatewayConfig, jwtUtils);
        assertNotNull(validator);
    }

    @Test
    void testValidate() throws InterruptedException {
        HourDeclarationRequest declarationRequest = new HourDeclarationRequest(1, 1, 10, "A");
        AsyncHiringValidator validator = new AsyncHiringValidator(gatewayConfig, jwtUtils);

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
        assertEquals(token, recordedRequest.getHeader(HttpHeaders.AUTHORIZATION));
    }

    @Test
    void testValidateAdminBypass() {
        // Pretend that the user is an admin (which allows them to bypass the validator)
        when(jwtUtils.getRole(jwsMock)).thenReturn(AsyncRoleValidator.Roles.ADMIN.name());

        // Configure test objects
        HourDeclarationRequest declarationRequest = new HourDeclarationRequest(1, 12, 10, "B");
        AsyncHiringValidator validator = new AsyncHiringValidator(gatewayConfig, jwtUtils);

        // Check the state
        Mono<Boolean> result = validator.validate(headers, declarationRequest.toJson());
        assertEquals(Boolean.TRUE, result.block());

        // Verify that no request to the course microservice was made
        assertEquals(0, mockWebServer.getRequestCount());
    }

    @Test
    void testValidateInvalidContract() throws InterruptedException {
        HourDeclarationRequest declarationRequest = new HourDeclarationRequest(1, 12, 10, "C");
        AsyncHiringValidator validator = new AsyncHiringValidator(gatewayConfig, jwtUtils);

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
        assertEquals(token, recordedRequest.getHeader(HttpHeaders.AUTHORIZATION));
    }

    @Test
    void testValidateNegativeHours() throws InterruptedException {
        HourDeclarationRequest declarationRequest = new HourDeclarationRequest(1, 20, 0, "D");
        AsyncHiringValidator validator = new AsyncHiringValidator(gatewayConfig, jwtUtils);

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
        assertEquals("/api/hiring-service/get-contract?courseID=20", recordedRequest.getPath());
        assertEquals(token, recordedRequest.getHeader(HttpHeaders.AUTHORIZATION));
    }

    @Test
    void testValidateMoreThanMaxHours() throws InterruptedException {
        HourDeclarationRequest declarationRequest = new HourDeclarationRequest(1, 3, 999, "E");
        AsyncHiringValidator validator = new AsyncHiringValidator(gatewayConfig, jwtUtils);

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
        assertEquals("/api/hiring-service/get-contract?courseID=3", recordedRequest.getPath());
        assertEquals(token, recordedRequest.getHeader(HttpHeaders.AUTHORIZATION));
    }

    @Test
    void testValidateEqualToMaxHours() throws InterruptedException {
        HourDeclarationRequest declarationRequest = new HourDeclarationRequest(1, 1, 15, "F");
        AsyncHiringValidator validator = new AsyncHiringValidator(gatewayConfig, jwtUtils);

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
        assertEquals(token, recordedRequest.getHeader(HttpHeaders.AUTHORIZATION));
    }

}
