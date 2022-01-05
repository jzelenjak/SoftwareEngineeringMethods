package nl.tudelft.sem.hiring.procedure.recommendation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import nl.tudelft.sem.hiring.procedure.entities.Application;
import nl.tudelft.sem.hiring.procedure.recommendation.entities.Recommendation;
import nl.tudelft.sem.hiring.procedure.recommendation.strategies.GradeStrategy;
import nl.tudelft.sem.hiring.procedure.recommendation.strategies.RecommendationStrategy;
import nl.tudelft.sem.hiring.procedure.repositories.ApplicationRepository;
import nl.tudelft.sem.hiring.procedure.utils.GatewayConfig;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest
class GradeStrategyTest {
    @Autowired
    private transient ApplicationRepository repo;

    private transient RecommendationStrategy strategy;

    private final transient LocalDateTime time = LocalDateTime.now();

    private transient MockWebServer mockWebServer;

    private transient ObjectMapper mapper;

    private static final transient String jwtToken = "mySecretToken";

    /**
     * A helper method used to verify simple recorded requests.
     *
     * @param request the request to be verified.
     * @param method  the expected method.
     */
    private void verifyRecordedRequest(RecordedRequest request, HttpMethod method) {
        Assertions.assertThat(request).isNotNull();
        Assertions.assertThat(request.getMethod()).isEqualTo(method.name());
        Assertions.assertThat(request.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo(jwtToken);
    }

    @BeforeEach
    void setup() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        HttpUrl url = mockWebServer.url("/api/courses/statistics/user-grade");

        GatewayConfig gatewayConfig = Mockito.mock(GatewayConfig.class);
        Mockito.when(gatewayConfig.getHost()).thenReturn(url.host());
        Mockito.when(gatewayConfig.getPort()).thenReturn(url.port());

        strategy = new GradeStrategy(repo, gatewayConfig, jwtToken);
        mapper = new ObjectMapper();

        // Clear the database
        repo.deleteAll();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void testRecommendSuccessful() throws JsonProcessingException, InterruptedException {
        // Prepare the repository and mockWebServer

        // Applicants No88, No81, No86; Course with courseId 27
        this.repo.save(new Application(88, 27L, time));
        this.repo.save(new Application(81, 27L, time));
        this.repo.save(new Application(86, 27L, time));

        String json = mapper.writeValueAsString(Map.of(88L, 9.1, 86L, 8.2, 81L, 7.5));
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(json));

        // Act and assert the result
        Assertions
                .assertThat(this.strategy.recommend(27L, 10, 0.0).block())
                .isEqualTo(List.of(new Recommendation(88L, 9.1),
                        new Recommendation(86L, 8.2),
                        new Recommendation(81L, 7.5)));

        RecordedRequest request = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        verifyRecordedRequest(request, HttpMethod.POST);
    }

    @Test
    void testRecommendNoApplicantsFound() throws InterruptedException {
        // Act and assert the result
        Assertions
                .assertThatThrownBy(() -> this.strategy.recommend(42L, 17, 0.0).block())
                .isInstanceOf(ResponseStatusException.class);

        Assertions.assertThat(mockWebServer.takeRequest(1, TimeUnit.SECONDS)).isNull();
    }

    @Test
    void testRecommendFailureAtCourses() throws InterruptedException {
        // Prepare the repository and mockWebServer

        // Applicant No44; Course with courseId 29
        this.repo.save(new Application(44, 29L, time));

        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        // Act and assert the result
        Assertions
                .assertThatThrownBy(() -> this.strategy.recommend(29L, 10, 0.0).block())
                .isInstanceOf(ResponseStatusException.class);

        RecordedRequest request = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        verifyRecordedRequest(request, HttpMethod.POST);
    }

    @Test
    void testRecommendEmptyBodyInResponseFromCourses() throws InterruptedException {
        // Prepare the repository and mockWebServer

        // Applicant No41; Course with courseId 23
        this.repo.save(new Application(41, 23L, time));

        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(""));

        // Act and assert the result
        Assertions
                .assertThatThrownBy(() -> this.strategy.recommend(23L, 10, 0.0).block())
                .isInstanceOf(ResponseStatusException.class);

        RecordedRequest request = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        verifyRecordedRequest(request, HttpMethod.POST);
    }

    @Test
    void testRecommendBadFormatInResponseFromCourses() throws InterruptedException {
        // Prepare the repository and mockWebServer

        // Applicant No44; Course with courseId 8
        this.repo.save(new Application(44, 8L, time));

        String json = mapper.createObjectNode().put("44", "FAIL").toString();

        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(json));

        // Act and assert the result
        Assertions
                .assertThatThrownBy(() -> this.strategy.recommend(8L, 10, 0.0).block())
                .isInstanceOf(ResponseStatusException.class);

        RecordedRequest request = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        verifyRecordedRequest(request, HttpMethod.POST);
    }

    @Test
    void testRecommendCannotMakeAnyRecommendations()
            throws JsonProcessingException, InterruptedException {
        // Prepare the repository and mockWebServer

        // Applicants No48, No49; Course with courseId 56789
        this.repo.save(new Application(48, 56789L, time));
        this.repo.save(new Application(49, 56789L, time));

        String json = mapper.writeValueAsString(Map.of());
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(json));

        // Act and assert the result
        Assertions
                .assertThatThrownBy(() -> this.strategy.recommend(56789L, 10, 9.5).block())
                .isInstanceOf(ResponseStatusException.class);

        RecordedRequest request = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        verifyRecordedRequest(request, HttpMethod.POST);
    }
}