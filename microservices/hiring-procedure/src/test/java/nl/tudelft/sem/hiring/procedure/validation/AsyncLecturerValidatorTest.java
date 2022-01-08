package nl.tudelft.sem.hiring.procedure.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import java.io.IOException;
import nl.tudelft.sem.hiring.procedure.utils.GatewayConfig;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
public class AsyncLecturerValidatorTest {

    private static final String AUTHORIZATION_TOKEN = "aMoGuS";

    private transient MockWebServer mockWebServer;

    private transient GatewayConfig gatewayConfig;

    private transient HttpHeaders headers;

    @MockBean
    private transient JwtUtils jwtUtils;

    @Mock
    private transient Jws<Claims> jwsMock;

    @BeforeEach
    private void setupEach() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        HttpUrl url = mockWebServer.url("/");
        gatewayConfig = Mockito.mock(GatewayConfig.class);
        when(gatewayConfig.getHost()).thenReturn(url.host());
        when(gatewayConfig.getPort()).thenReturn(url.port());

        when(jwtUtils.resolveToken(anyString())).thenReturn("");
        when(jwtUtils.validateAndParseClaims(anyString())).thenReturn(jwsMock);
        when(jwtUtils.getRole(jwsMock)).thenReturn("LECTURER");

        headers = Mockito.mock(HttpHeaders.class);
        when(headers.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn(AUTHORIZATION_TOKEN);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void testConstructor() {
        AsyncLecturerValidator validator = new AsyncLecturerValidator(jwtUtils, gatewayConfig,
                1337L);
        assertNotNull(validator);
    }

    @Test
    void testValidate() throws InterruptedException {
        when(jwtUtils.getUserId(jwsMock)).thenReturn(12L);
        AsyncLecturerValidator validator = new AsyncLecturerValidator(jwtUtils, gatewayConfig,
                1337L);

        // Enqueue response that tells us that the user teaches the course
        // associated to the declaration ID
        mockWebServer.enqueue(new MockResponse().setResponseCode(HttpStatus.OK.value()));

        // check the state
        Mono<Boolean> result = validator.validate(headers, "");
        assertEquals(Boolean.TRUE, result.block());

        // check that request was made to correct place
        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertThat(recordedRequest).isNotNull();
        assertEquals(HttpMethod.GET.name(), recordedRequest.getMethod());
        assertEquals("/api/courses/get/teaches/12/1337", recordedRequest.getPath());
        assertEquals(AUTHORIZATION_TOKEN, recordedRequest.getHeader(HttpHeaders.AUTHORIZATION));
    }

    @Test
    void testValidateAdminBypass() {
        // Pretend that the user is an admin (which allows them to bypass the validator)
        when(jwtUtils.getRole(jwsMock)).thenReturn(AsyncRoleValidator.Roles.ADMIN.name());
        AsyncLecturerValidator validator = new AsyncLecturerValidator(jwtUtils, gatewayConfig,
                1337L);

        // Check the state
        Mono<Boolean> result = validator.validate(headers, "");
        assertEquals(Boolean.TRUE, result.block());

        // Verify that no request to the course microservice was made
        assertEquals(0, mockWebServer.getRequestCount());
    }

    @Test
    void testValidateInvalid() throws InterruptedException {
        when(jwtUtils.getUserId(jwsMock)).thenReturn(12L);
        AsyncLecturerValidator validator = new AsyncLecturerValidator(jwtUtils, gatewayConfig,
                1337L);

        // Enqueue response that tells us that the user teaches the course
        // associated to the declaration ID
        mockWebServer.enqueue(new MockResponse().setResponseCode(HttpStatus.NOT_FOUND.value()));

        // check the state
        Mono<Boolean> result = validator.validate(headers, "");
        assertThatExceptionOfType(ResponseStatusException.class).isThrownBy(result::block);

        // check that request was made to correct place
        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertThat(recordedRequest).isNotNull();
        assertEquals(HttpMethod.GET.name(), recordedRequest.getMethod());
        assertEquals("/api/courses/get/teaches/12/1337", recordedRequest.getPath());
        assertEquals(AUTHORIZATION_TOKEN, recordedRequest.getHeader(HttpHeaders.AUTHORIZATION));
    }
}
