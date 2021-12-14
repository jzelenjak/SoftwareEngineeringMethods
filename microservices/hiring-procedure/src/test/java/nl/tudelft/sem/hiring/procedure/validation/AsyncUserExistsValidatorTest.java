package nl.tudelft.sem.hiring.procedure.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.google.gson.JsonObject;
import java.io.IOException;
import nl.tudelft.sem.hiring.procedure.utils.GatewayConfig;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
public class AsyncUserExistsValidatorTest {
    @MockBean
    private transient GatewayConfig gatewayConfigMock;

    private static MockWebServer mockWebServer;

    @BeforeAll
    private static void setup() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @BeforeEach
    private void setupEach() {
        HttpUrl url = mockWebServer.url("/");
        when(gatewayConfigMock.getHost()).thenReturn(url.host());
        when(gatewayConfigMock.getPort()).thenReturn(url.port());
    }

    @AfterAll
    private static void teardown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    public void testConstructor() {
        AsyncUserExistsValidator validator = new AsyncUserExistsValidator(gatewayConfigMock, 42);
        assertNotNull(validator);
    }

    @Test
    public void testValidate() {
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
        Boolean result = validator.validate(null, requestBody)
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
        Boolean result = validator.validate(null, requestBody)
            .onErrorReturn(false)
            .block();
        assertNotNull(result);
        assertFalse(result);
    }
}
