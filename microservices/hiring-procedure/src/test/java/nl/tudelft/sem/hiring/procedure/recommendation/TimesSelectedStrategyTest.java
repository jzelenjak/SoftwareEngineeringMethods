package nl.tudelft.sem.hiring.procedure.recommendation;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import nl.tudelft.sem.hiring.procedure.entities.Application;
import nl.tudelft.sem.hiring.procedure.entities.ApplicationStatus;
import nl.tudelft.sem.hiring.procedure.recommendation.entities.Recommendation;
import nl.tudelft.sem.hiring.procedure.recommendation.strategies.RecommendationStrategy;
import nl.tudelft.sem.hiring.procedure.recommendation.strategies.TimesSelectedStrategy;
import nl.tudelft.sem.hiring.procedure.repositories.ApplicationRepository;
import nl.tudelft.sem.hiring.procedure.utils.GatewayConfig;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class TimesSelectedStrategyTest {

    private static transient MockWebServer mockWebServer;

    @Autowired
    private transient ApplicationRepository repo;

    private transient RecommendationStrategy strategy;

    private final transient LocalDateTime time = LocalDateTime.now();

    private transient GatewayConfig gatewayConfig;

    private transient ObjectMapper mapper;

    private static final transient String GET_ALL_EDITIONS = "/api/courses/get-all-editions";

    void configureGateway(String path) {
        HttpUrl url = mockWebServer.url(path);
        Mockito.when(gatewayConfig.getHost()).thenReturn(url.host());
        Mockito.when(gatewayConfig.getPort()).thenReturn(url.port());
    }

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @BeforeEach
    void setup() {
        this.gatewayConfig = Mockito.mock(GatewayConfig.class);
        this.strategy = new TimesSelectedStrategy(repo, gatewayConfig);
        this.mapper = new ObjectMapper();
    }

    @Test
    void testRecommendManyApplicantsSuccessful() throws InterruptedException {
        // Course with courseId 69 has the same course code as 66, 62, 63, 65, 64
        // Number of applicants
        String input = "5\n"
                // Applicant No42; selected in the past: 3 times
                + "4\n42 69 IN_PROGRESS 42 66 ACCEPTED 42 64 ACCEPTED 42 62 ACCEPTED\n"
                // Applicant No43; selected in the past: 1 time
                + "3\n43 64 ACCEPTED 43 69 IN_PROGRESS 43 66 REJECTED\n"
                // Applicant No44; selected in the past 2 times
                + "4\n44 65 ACCEPTED 44 63 ACCEPTED 44 69 IN_PROGRESS 44 62 WITHDRAWN\n"
                // Not an Applicant No45; selected in the past: 3 times
                + "4\n45 66 ACCEPTED 45 63 ACCEPTED 45 65 ACCEPTED 45 43 ACCEPTED\n"
                // Applicant No46; selected in the past: 0 times
                + "1\n46 69 IN_PROGRESS\n";
        addApplications(input);

        configureGateway(GET_ALL_EDITIONS + "?courseId=69");

        String json = mapper.createObjectNode()
                .set("courseIds", mapper.valueToTree(List.of(66L, 62L, 63L, 65L, 64L))).toString();

        mockWebServer
            .enqueue(new MockResponse().setResponseCode(200).setBody(json));

        Assertions
                .assertThat(this.strategy.recommend(69L, 10, 0.0).block())
                .isEqualTo(List.of(new Recommendation(42L, 3),
                        new Recommendation(44L, 2),
                        new Recommendation(43L, 1)));

        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        Assertions
                .assertThat(recordedRequest)
                .isNotNull();
        Assertions
                .assertThat(recordedRequest.getMethod()).isEqualTo("GET");
    }

    /** A helper method to add applications to the repository.
     *
     * @param input the input string in the following format:
     *              N            (total number of users)
     *              n            (number of applications for the following user)
     *              userId courseId1 STATUS1 userId courseId2 STATUS2 ...
     *              ...
     *
     *      I needed to duplicate userId for each application, because PMD was complaining
     *          about DU anomalies since I was accessing the userId variable outside the for-loop.
     */
    private void addApplications(String input) {
        Scanner sc = new Scanner(input);
        try (sc) {
            int numberOfApplications = sc.nextInt();
            sc.nextLine();
            for (int i = 0; i < numberOfApplications; ++i) {
                int applications = sc.nextInt();
                sc.nextLine();
                for (int j = 0; j < applications; ++j) {
                    Application appl = new Application(sc.nextLong(), sc.nextLong(), time);
                    appl.setStatus(ApplicationStatus.valueOf(sc.next()));
                    this.repo.save(appl);
                }
                sc.nextLine();
            }
        }
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }
}
