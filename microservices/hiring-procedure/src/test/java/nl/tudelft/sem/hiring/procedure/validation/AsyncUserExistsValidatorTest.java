package nl.tudelft.sem.hiring.procedure.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import nl.tudelft.sem.hiring.procedure.utils.GatewayConfig;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
public class AsyncUserExistsValidatorTest {

    private static final String AUTHORIZATION_TOKEN = "MyToken";

    @MockBean
    private transient GatewayConfig gatewayConfigMock;

    private transient MockWebServer mockWebServer;
    private transient HttpHeaders mockHeaders;

    @BeforeEach
    private void setupEach() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        HttpUrl url = mockWebServer.url("/");
        when(gatewayConfigMock.getHost()).thenReturn(url.host());
        when(gatewayConfigMock.getPort()).thenReturn(url.port());

        mockHeaders = Mockito.mock(HttpHeaders.class);
        when(mockHeaders.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn(AUTHORIZATION_TOKEN);
    }

    @AfterEach
    private void teardown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    public void testConstructor() {
        AsyncUserExistsValidator validator = new AsyncUserExistsValidator(gatewayConfigMock, 42);
        assertNotNull(validator);
    }

    @Test
    public void testValidate() throws InterruptedException {
        // Construct validator instance and courseId object
        final long userId = 521234;
        final AsyncUserExistsValidator validator = new AsyncUserExistsValidator(
            gatewayConfigMock, userId);

        // Enqueue a response
        mockWebServer.enqueue(new MockResponse().setResponseCode(HttpStatus.OK.value()));

        // Construct the json object used for testing
        JsonObject json = new JsonObject();
        json.addProperty("userId", userId);
        String requestBody = json.toString();

        // Perform the validation
        Boolean result = validator.validate(mockHeaders, requestBody)
            .onErrorReturn(false)
            .block();
        assertNotNull(result);
        assertTrue(result);
    }

    @Test
    public void testNonExistentCourse() {
        // Construct validator instance and courseId object
        final long userId = 521234;
        final AsyncUserExistsValidator validator = new AsyncUserExistsValidator(
            gatewayConfigMock, userId);

        // Enqueue a response
        mockWebServer.enqueue(new MockResponse().setResponseCode(HttpStatus.NOT_FOUND.value()));

        // Construct the json object used for testing
        JsonObject json = new JsonObject();
        json.addProperty("userId", userId);
        String requestBody = json.toString();

        // Perform the validation
        Boolean result = validator.validate(mockHeaders, requestBody)
            .onErrorReturn(false)
            .block();
        assertNotNull(result);
        assertFalse(result);
    }
}
