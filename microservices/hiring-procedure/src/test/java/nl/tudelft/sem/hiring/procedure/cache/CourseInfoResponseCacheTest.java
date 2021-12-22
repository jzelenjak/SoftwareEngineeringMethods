package nl.tudelft.sem.hiring.procedure.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import nl.tudelft.sem.hiring.procedure.utils.GatewayConfig;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@SpringBootTest(classes = CourseInfoResponseCache.class)
public class CourseInfoResponseCacheTest {

    @Autowired
    private transient CourseInfoResponseCache cache;

    @MockBean
    private transient GatewayConfig gatewayConfigMock;

    private transient MockWebServer mockWebServer;

    @BeforeEach
    private void setupEach() throws IOException {
        // Setup mock web server
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        // Setup gateway config
        HttpUrl url = mockWebServer.url("/");
        when(gatewayConfigMock.getHost()).thenReturn(url.host());
        when(gatewayConfigMock.getPort()).thenReturn(url.port());

        // Invalidate the cache each test
        cache.invalidateCache();
    }

    @AfterEach
    private void teardown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    public void testGetCourseInfoNonExisting() throws InterruptedException {
        // Enqueue response and setup parameters
        long courseId = 1337;
        mockWebServer.enqueue(new MockResponse().setResponseCode(HttpStatus.NOT_FOUND.value()));

        // Fetch course information
        Mono<String> courseInfo = cache.getCourseInfoResponse(courseId);

        // Verify that the response object is not available
        assertThrows(ResponseStatusException.class, courseInfo::block);

        // Verify the recorded request
        RecordedRequest request = mockWebServer.takeRequest(10, TimeUnit.MILLISECONDS);
        assertNotNull(request);
        assertEquals(HttpMethod.GET.name(), request.getMethod());
        assertEquals("/api/courses/get/" + courseId, request.getPath());
    }

    @Test
    public void testGetCourseInfoOnce() throws InterruptedException {
        // Enqueue response and setup parameters
        long courseId = 1337;
        mockWebServer.enqueue(new MockResponse().setBody(new JsonObject().toString()));

        // Fetch course information
        Mono<String> courseInfo = cache.getCourseInfoResponse(courseId);

        // Verify that the response object is not available
        assertEquals(new JsonObject().toString(), courseInfo.block());

        // Verify the recorded request
        RecordedRequest request = mockWebServer.takeRequest(10, TimeUnit.MILLISECONDS);
        assertNotNull(request);
        assertEquals(HttpMethod.GET.name(), request.getMethod());
        assertEquals("/api/courses/get/" + courseId, request.getPath());
    }

    @Test
    public void testGetCourseInfoMultiple() throws InterruptedException {
        // Enqueue response and setup parameters
        List<Long> courseIds = List.of(1337L, 1338L, 1339L, 1340L, 1341L, 1342L);
        List<String> responses = new ArrayList<>();

        courseIds.forEach(courseId -> {
            JsonObject json = new JsonObject();
            json.addProperty("id", courseId);
            String serialized = json.toString();

            responses.add(serialized);
            mockWebServer.enqueue(new MockResponse().setBody(serialized));
        });

        // Fetch course information
        int repeat = 10;
        for (int i = 0; i < repeat; i++) {
            for (int j = 0; j < courseIds.size(); j++) {
                assertEquals(responses.get(j),
                        cache.getCourseInfoResponse(courseIds.get(j)).block());
            }
        }

        // Verify that the info was fetched only once for every course ID
        assertEquals(courseIds.size(), mockWebServer.getRequestCount());
    }

}
