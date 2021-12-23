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
import nl.tudelft.sem.hiring.procedure.recommendation.strategies.HoursStrategy;
import nl.tudelft.sem.hiring.procedure.recommendation.strategies.RecommendationStrategy;
import nl.tudelft.sem.hiring.procedure.repositories.ApplicationRepository;
import nl.tudelft.sem.hiring.procedure.utils.GatewayConfig;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.Dispatcher;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class HoursStrategyTest {
    @Autowired
    private transient ApplicationRepository repo;

    private transient RecommendationStrategy strategy;

    private final transient LocalDateTime time = LocalDateTime.now();

    private transient MockWebServer mockWebServer;

    private transient ObjectMapper mapper;

    private static final transient String COURSE_IDS = "courseIds";
    private static final transient String GET_ALL_EDITIONS = "/api/courses/get-all-editions";
    private static final transient String GET_HOURS_STATS = "/api/hour-management"
            + "/statistics/total-user-hours";
    private static final transient String jwtToken = "mySecretToken";

    /**
     * A helper method that is used to handle the sequence of responses.
     * It is used when communication with both Courses and Hour Management
     * microservices takes place (in those test cases).
     * Credits to:
     * https://stackoverflow.com/questions/54458960/test-with-consecutive-calls-to-mockwebserver
     *
     * @param jsonFromCourses json response body from Courses microservice
     * @param jsonFromHours   (possibly empty) json response body from Hour Management microservice
     * @param codeFromHours   the status code from Hour Management microservice
     * @return the created dispatcher that will receive a request, check its URL's
     *         endpoint and return corresponding response.
     */
    private Dispatcher dispatcher(String jsonFromCourses, String jsonFromHours, int codeFromHours) {
        return new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                if (request.getPath().contains(GET_ALL_EDITIONS)) {
                    return new MockResponse().setResponseCode(200).setBody(jsonFromCourses);
                }
                if (request.getPath().contains(GET_HOURS_STATS)) {
                    if (jsonFromHours == null) {
                        return new MockResponse().setResponseCode(codeFromHours);
                    }
                    return new MockResponse().setResponseCode(codeFromHours).setBody(jsonFromHours);
                }
                return new MockResponse().setResponseCode(404);
            }
        };
    }

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
        HttpUrl url = mockWebServer.url("/");

        GatewayConfig gatewayConfig = Mockito.mock(GatewayConfig.class);
        Mockito.when(gatewayConfig.getHost()).thenReturn(url.host());
        Mockito.when(gatewayConfig.getPort()).thenReturn(url.port());

        strategy = new HoursStrategy(repo, gatewayConfig, jwtToken);
        mapper = new ObjectMapper();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void testRecommendSuccessful() throws JsonProcessingException, InterruptedException {
        // Prepare the repository, mockWebServer and gatewayConfig

        // Applicants No36,37,38; Course with courseId 6666 (same code as 6665)
        this.repo.save(new Application(36L, 6666L, time));
        this.repo.save(new Application(37L, 6666L, time));
        this.repo.save(new Application(38L, 6666L, time));

        String jsonFromCourses = mapper.createObjectNode()
                .set(COURSE_IDS, mapper.valueToTree(List.of(6666L, 6665L))).toString();
        String jsonFromHours = mapper
                .writeValueAsString(Map.of(36L, 101.0, 38L, 135.0));

        mockWebServer.setDispatcher(dispatcher(jsonFromCourses, jsonFromHours, 200));

        // Act and assert the result
        Assertions
                .assertThat(this.strategy.recommend(6666L, 2, 0.0).block())
                .isEqualTo(List.of(new Recommendation(38L, 135.0),
                        new Recommendation(36L, 101.0)));

        RecordedRequest requestCourses = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        verifyRecordedRequest(requestCourses, HttpMethod.GET);
        RecordedRequest requestHours = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        verifyRecordedRequest(requestHours, HttpMethod.POST);
    }

    @Test
    void testRecommendNoApplicantsFound() throws InterruptedException {
        // Act and assert the result
        Assertions
                .assertThatThrownBy(() -> this.strategy.recommend(69L, 42, 0.0).block())
                .isInstanceOf(ResponseStatusException.class);

        Assertions.assertThat(mockWebServer.takeRequest(1, TimeUnit.SECONDS)).isNull();
        Assertions.assertThat(mockWebServer.takeRequest(1, TimeUnit.SECONDS)).isNull();
    }

    @Test
    void testRecommendFailureAtCourses() throws InterruptedException {
        // Prepare the repository, mockWebServer and gatewayConfig

        // Applicants No74, No59; Course with courseId 29 (same code as 20)
        this.repo.save(new Application(74L, 29L, time));
        this.repo.save(new Application(59L, 29L, time));

        mockWebServer.enqueue(new MockResponse().setResponseCode(500));


        // Act and assert the result
        Assertions
                .assertThatThrownBy(() -> this.strategy.recommend(29L, 6, 0.0).block())
                .isInstanceOf(ResponseStatusException.class);

        RecordedRequest request = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        verifyRecordedRequest(request, HttpMethod.GET);
        Assertions.assertThat(mockWebServer.takeRequest(1, TimeUnit.SECONDS)).isNull();
    }

    @Test
    void testRecommendEmptyBodyInResponseFromCourses() throws InterruptedException {
        // Prepare the repository, mockWebServer and gatewayConfig

        // Applicant No112; Course with courseId 666 (same code as 665)
        this.repo.save(new Application(112L, 666L, time));

        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(""));


        // Act and assert the result
        Assertions
                .assertThatThrownBy(() -> this.strategy.recommend(666L, 7, 0.0).block())
                .isInstanceOf(ResponseStatusException.class);

        RecordedRequest request = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        verifyRecordedRequest(request, HttpMethod.GET);
        Assertions.assertThat(mockWebServer.takeRequest(1, TimeUnit.SECONDS)).isNull();
    }

    @Test
    void testRecommendWrongFieldInResponseFromCourses() throws InterruptedException {
        // Prepare the repository, mockWebServer and gatewayConfig

        // Applicant No112; Course with courseId 666 (same code as 665)
        this.repo.save(new Application(112L, 666L, time));

        String json = mapper.createObjectNode()
                .set("courseIDs", mapper.valueToTree(List.of(666L, 665L))).toString();
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(json));


        // Act and assert the result
        Assertions
                .assertThatThrownBy(() -> this.strategy.recommend(666L, 9, 0.0).block())
                .isInstanceOf(ResponseStatusException.class);

        RecordedRequest request = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        verifyRecordedRequest(request, HttpMethod.GET);
        Assertions.assertThat(mockWebServer.takeRequest(1, TimeUnit.SECONDS)).isNull();
    }

    @Test
    void testRecommendBadFormatInResponseFromCourses() throws InterruptedException {
        // Prepare the repository, mockWebServer and gatewayConfig

        // Applicant No1234; Course with courseId 555 (same code as 554)
        this.repo.save(new Application(1234L, 555L, time));

        String json = mapper.createObjectNode().put(COURSE_IDS, "AMOGUS").toString();
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(json));


        // Act and assert the result
        Assertions
                .assertThatThrownBy(() -> this.strategy.recommend(555L, 3, 0.0).block())
                .isInstanceOf(ResponseStatusException.class);

        RecordedRequest request = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        verifyRecordedRequest(request, HttpMethod.GET);
        Assertions.assertThat(mockWebServer.takeRequest(1, TimeUnit.SECONDS)).isNull();
    }

    @Test
    void testRecommendFailureAtHours() throws InterruptedException {
        // Prepare the repository, mockWebServer and gatewayConfig

        // Applicant No5432; Course with courseId 532 (same code as 531)
        this.repo.save(new Application(5432L, 532L, time));

        String json = mapper.createObjectNode()
                .set(COURSE_IDS, mapper.valueToTree(List.of(532L, 531L))).toString();

        mockWebServer.setDispatcher(dispatcher(json, null, 500));

        // Act and assert the result
        Assertions
                .assertThatThrownBy(() -> this.strategy.recommend(532L, 1, 0.0).block())
                .isInstanceOf(ResponseStatusException.class);

        RecordedRequest requestCourses = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        verifyRecordedRequest(requestCourses, HttpMethod.GET);
        RecordedRequest requestHours = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        verifyRecordedRequest(requestHours, HttpMethod.POST);
    }

    @Test
    void testRecommendEmptyBodyInResponseFromHours() throws InterruptedException {
        // Prepare the repository, mockWebServer and gatewayConfig

        // Applicant No80; Course with courseId 8080 (same code as 8088)
        this.repo.save(new Application(80L, 8080L, time));

        String jsonFromCourses = mapper.createObjectNode()
                .set(COURSE_IDS, mapper.valueToTree(List.of(8080L, 8088L))).toString();

        mockWebServer.setDispatcher(dispatcher(jsonFromCourses, "", 200));

        // Act and assert the result
        Assertions
                .assertThatThrownBy(() -> this.strategy.recommend(8080L, 42, 0.0).block())
                .isInstanceOf(ResponseStatusException.class);

        RecordedRequest requestCourses = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        verifyRecordedRequest(requestCourses, HttpMethod.GET);
        RecordedRequest requestHours = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        verifyRecordedRequest(requestHours, HttpMethod.POST);
    }

    @Test
    void testRecommendBadFormatInResponseFromHours() throws InterruptedException {
        // Prepare the repository, mockWebServer and gatewayConfig

        // Applicant No88; Course with courseId 8088 (same code as 8080)
        this.repo.save(new Application(88L, 8088L, time));

        String jsonFromCourses = mapper.createObjectNode()
                .set(COURSE_IDS, mapper.valueToTree(List.of(8088L, 8080L))).toString();
        String jsonFromHours = mapper.createObjectNode().put("88", "FAIL").toString();

        mockWebServer.setDispatcher(dispatcher(jsonFromCourses, jsonFromHours, 200));

        // Act and assert the result
        Assertions
                .assertThatThrownBy(() -> this.strategy.recommend(8088L, 69, 0.0).block())
                .isInstanceOf(ResponseStatusException.class);

        RecordedRequest requestCourses = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        verifyRecordedRequest(requestCourses, HttpMethod.GET);
        RecordedRequest requestHours = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        verifyRecordedRequest(requestHours, HttpMethod.POST);
    }

    @Test
    void testRecommendCannotMakeAnyRecommendations()
            throws InterruptedException, JsonProcessingException {
        // Prepare the repository, mockWebServer and gatewayConfig

        // Applicant No81; Course with courseId 443 (same code as 80)
        this.repo.save(new Application(81L, 443L, time));

        String jsonFromCourses = mapper.createObjectNode()
                .set(COURSE_IDS, mapper.valueToTree(List.of(443L, 80L))).toString();
        String jsonFromHours = mapper.writeValueAsString(Map.of());

        mockWebServer.setDispatcher(dispatcher(jsonFromCourses, jsonFromHours, 200));

        // Act and assert the result
        Assertions
                .assertThatThrownBy(() -> this.strategy.recommend(443L, 3, 200.0).block())
                .isInstanceOf(ResponseStatusException.class);

        RecordedRequest requestCourses = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        verifyRecordedRequest(requestCourses, HttpMethod.GET);
        RecordedRequest requestHours = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        verifyRecordedRequest(requestHours, HttpMethod.POST);
    }
}
