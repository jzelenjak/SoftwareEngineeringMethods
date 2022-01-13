package nl.tudelft.sem.hiring.procedure.recommendation;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import nl.tudelft.sem.hiring.procedure.entities.Submission;
import nl.tudelft.sem.hiring.procedure.entities.SubmissionStatus;
import nl.tudelft.sem.hiring.procedure.recommendation.entities.Recommendation;
import nl.tudelft.sem.hiring.procedure.recommendation.strategies.Recommender;
import nl.tudelft.sem.hiring.procedure.recommendation.strategies.TimesSelectedStrategy;
import nl.tudelft.sem.hiring.procedure.repositories.SubmissionRepository;
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
public class TimesSelectedStrategyTest {

    @Autowired
    private transient SubmissionRepository repo;

    private transient Recommender strategy;

    private final transient LocalDateTime time = LocalDateTime.now();

    private transient MockWebServer mockWebServer;

    private transient GatewayConfig gatewayConfig;

    private transient ObjectMapper mapper;

    private static final transient String ONE_LF = "1\n";
    private static final transient String TWO_LF = "2\n";
    private static final transient String GET_ALL_EDITIONS = "/api/courses/get-all-editions";
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

    void configureGateway(String path) {
        HttpUrl url = mockWebServer.url(path);
        Mockito.when(gatewayConfig.getHost()).thenReturn(url.host());
        Mockito.when(gatewayConfig.getPort()).thenReturn(url.port());
    }

    @BeforeEach
    void setup() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        gatewayConfig = Mockito.mock(GatewayConfig.class);
        strategy = new TimesSelectedStrategy(repo, gatewayConfig, jwtToken);
        mapper = new ObjectMapper();

        // Clear the database
        repo.deleteAll();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    /**
     * Tests involving mockWebServer.
     */

    @Test
    void testRecommendManyApplicantsSuccessful() throws InterruptedException {
        // Prepare the repository, mockWebServer and gatewayConfig

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
            .set("courseIds", mapper.valueToTree(List.of(69L, 66L, 62L, 63L, 65L, 64L))).toString();

        mockWebServer
            .enqueue(new MockResponse().setResponseCode(200).setBody(json));

        // Act and assert the result
        Assertions
            .assertThat(this.strategy.recommend(69L, 10, 0.0).block())
            .isEqualTo(List.of(new Recommendation(42L, 3),
                new Recommendation(44L, 2),
                new Recommendation(43L, 1)));

        RecordedRequest request = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        verifyRecordedRequest(request, HttpMethod.GET);
    }

    @Test
    void testRecommendInvalidResponseFromCourses() throws InterruptedException {
        // Prepare the repository, mockWebServer and gatewayConfig

        // Course with courseId 69 has the same course code as 66, 62, 64
        // Number of applicants; Applicant No41; selected in the past: 2 times
        String input = ONE_LF + "3\n41 69 IN_PROGRESS 41 66 ACCEPTED 41 64 ACCEPTED\n";
        addApplications(input);

        configureGateway(GET_ALL_EDITIONS + "?courseId=69");

        String json = mapper.createObjectNode()
            .put("courseIds", "BLABLABLA").toString();

        mockWebServer
            .enqueue(new MockResponse().setResponseCode(200).setBody(json));


        // Act and assert the result
        Assertions
            .assertThatThrownBy(() -> this.strategy.recommend(69L, 10, 0.0).block())
            .isInstanceOf(ResponseStatusException.class);

        RecordedRequest request = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        verifyRecordedRequest(request, HttpMethod.GET);
    }

    @Test
    void testRecommendWrongFieldNameInResponseFromCourses() throws InterruptedException {
        // Prepare the repository, mockWebServer and gatewayConfig

        // Course with courseId 69 has the same course code as 66
        // Number of applicants; Applicant No42; selected in the past: 1 time
        String input = ONE_LF + "2\n42 69 IN_PROGRESS 42 66 ACCEPTED\n";
        addApplications(input);

        configureGateway(GET_ALL_EDITIONS + "?courseId=69");

        String json = mapper.createObjectNode()
            .set("courseIDs", mapper.valueToTree(List.of(69L, 66L))).toString();

        mockWebServer
            .enqueue(new MockResponse().setResponseCode(200).setBody(json));


        // Act and assert the result
        Assertions
            .assertThatThrownBy(() -> this.strategy.recommend(69L, 10, 0.0).block())
            .isInstanceOf(ResponseStatusException.class);

        RecordedRequest request = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        verifyRecordedRequest(request, HttpMethod.GET);
    }

    @Test
    void testRecommendFailureAtCourses() throws InterruptedException {
        // Prepare the repository, mockWebServer and gatewayConfig

        // Course with courseId 22 has the same course code as 21
        // Number of applicants; Applicant No42; selected in the past: 0 times
        String input = ONE_LF + "1\n42 22 IN_PROGRESS\n";
        addApplications(input);

        configureGateway(GET_ALL_EDITIONS + "?courseId=22");

        mockWebServer
            .enqueue(new MockResponse().setResponseCode(500));


        // Act and assert the result
        Assertions
            .assertThatThrownBy(() -> this.strategy.recommend(22L, 6, 0.0).block())
            .isInstanceOf(ResponseStatusException.class);

        RecordedRequest request = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        verifyRecordedRequest(request, HttpMethod.GET);
    }

    @Test
    void testRecommendNoApplicants() throws InterruptedException {
        // Act and assert the result
        Assertions
            .assertThatThrownBy(() -> this.strategy.recommend(17L, 4, 0.0).block())
            .isInstanceOf(ResponseStatusException.class);

        Assertions.assertThat(mockWebServer.takeRequest(1, TimeUnit.SECONDS)).isNull();
    }

    @Test
    void testCannotMakeAnyRecommendations() throws InterruptedException {
        // Prepare the repository, mockWebServer and gatewayConfig

        // Course with courseId 58 has the same course code as 59
        // Number of applicants; Applicant No42; selected in the past: 1 time
        String input = ONE_LF + "2\n42 58 IN_PROGRESS 42 58 ACCEPTED\n";
        addApplications(input);

        configureGateway(GET_ALL_EDITIONS + "?courseId=58");

        String json = mapper.createObjectNode()
            .set("courseIds", mapper.valueToTree(List.of(58L, 59L))).toString();

        mockWebServer
            .enqueue(new MockResponse().setResponseCode(200).setBody(json));


        // Act and assert the result
        Assertions
            .assertThatThrownBy(() -> this.strategy.recommend(58L, 10, 10.0).block())
            .isInstanceOf(ResponseStatusException.class);

        RecordedRequest request = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        verifyRecordedRequest(request, HttpMethod.GET);
    }

    /**
     * Tests for query logic. Tested separately to avoid the overhead caused by
     * network communication that happens in TimesSelectedStrategy.
     * I have made the method recommendFromRepo public to be able to test it,
     * and also because it can exist without other methods in TimesSelectedStrategy class
     * as long as a list of course IDs with the same course code is provided.
     */

    @Test
    void testRecommendFromRepoUserWhoHasNotAppliedMustBeIgnored() {
        // Course with courseId 32 has the same course code as 26, 12, 41, 40
        // Number of applicants
        String input = "3\n"
            // Not an applicant No42; selected in the past: 3 times
            + "4\n42 31 IN_PROGRESS 42 12 ACCEPTED 42 41 ACCEPTED 42 40 ACCEPTED\n"
            // Applicant No69; selected in the past: 1 time
            + "3\n69 12 ACCEPTED 69 32 IN_PROGRESS 69 26 REJECTED\n"
            // Applicant No256; selected in the past 2 times
            + "4\n256 26 ACCEPTED 256 12 ACCEPTED 256 32 IN_PROGRESS 256 40 WITHDRAWN\n";
        addApplications(input);

        List<Long> courseIds = List.of(32L, 26L, 12L, 41L, 40L);
        Assertions
            .assertThat(((TimesSelectedStrategy) this.strategy)
                .recommendFromRepo(32L, courseIds, 4, 0.0))
            .isEqualTo(List.of(new Recommendation(256L, 2.0),
                new Recommendation(69L, 1L)));
    }

    @Test
    void testRecommendFromRepoUserWithZeroTimesSelectedMustNotBeRecommended() {
        // Course with courseId 11 has the same course code as 9 an 10
        // Number of applicants
        String input = ONE_LF
            // Applicant No46; selected in the past: 0 times
            + "1\n46 69 IN_PROGRESS\n";
        addApplications(input);

        List<Long> courseIds = List.of(11L, 10L, 9L);
        Assertions
            .assertThat(((TimesSelectedStrategy) this.strategy)
                .recommendFromRepo(11L, courseIds, 6, 0.0))
            .isEmpty();
    }

    @Test
    void testRecommendFromRepoRejectedApplicationMustBeIgnored() {
        // Course with courseId 69 has the same course code as 66, 64, 65
        // Number of applicants
        String input = ONE_LF
            // Applicant No43; selected in the past: 1 time
            + "4\n43 65 REJECTED 43 64 ACCEPTED 43 69 IN_PROGRESS 43 66 REJECTED\n";
        addApplications(input);

        List<Long> courseIds = List.of(69L, 66L, 64L, 65L);
        Assertions
            .assertThat(((TimesSelectedStrategy) this.strategy)
                .recommendFromRepo(69L, courseIds, 4, 0.0))
            .isEqualTo(List.of(new Recommendation(43L, 1.0)));
    }

    @Test
    void testRecommendFromRepoWithdrawnApplicationMustBeIgnored() {
        // Course with courseId 113 has the same course code as 116, 112, 113, 114
        // Number of applicants
        String input = ONE_LF
            // Applicant No45; selected in the past 1 time
            + "4\n45 112 ACCEPTED 45 114 WITHDRAWN 45 113 IN_PROGRESS 45 114 WITHDRAWN\n";
        addApplications(input);

        List<Long> courseIds = List.of(113L, 116L, 112L, 113L, 114L);
        Assertions
            .assertThat(((TimesSelectedStrategy) this.strategy)
                .recommendFromRepo(113L, courseIds, 4, 0.0))
            .isEqualTo(List.of(new Recommendation(45L, 1.0)));
    }

    @Test
    void testRecommendFromRepoApplicantsForOtherCoursesMustBeIgnored() {
        // Course with courseId 26 has the same course code as 32, 33, 35, 41
        // Number of applicants
        String input = TWO_LF
            // Not an applicant No42; selected in the past: 3 times
            + "4\n42 27 IN_PROGRESS 42 32 ACCEPTED 42 35 ACCEPTED 42 41 ACCEPTED\n"
            // Not an applicant No45; selected in the past: 1 time
            + "4\n45 25 IN_PROGRESS 45 41 ACCEPTED 45 65 ACCEPTED 45 68 ACCEPTED\n";
        addApplications(input);

        List<Long> courseIds = List.of(26L, 32L, 33L, 35L, 41L);
        Assertions
            .assertThat(((TimesSelectedStrategy) this.strategy)
                .recommendFromRepo(26L, courseIds, 4, 0.0))
            .isEmpty();
    }

    @Test
    void testRecommendFromRepoCanOnlySelectLimitedNumber() {
        // Course with courseId 69 has the same course code as 66, 62, 63, 65, 64
        // Number of applicants
        String input = "4\n"
            // Applicant No42; selected in the past: 3 times
            + "4\n42 69 IN_PROGRESS 42 66 ACCEPTED 42 64 ACCEPTED 42 62 ACCEPTED\n"
            // Applicant No43; selected in the past: 1 time
            + "3\n43 64 ACCEPTED 43 69 IN_PROGRESS 43 66 REJECTED\n"
            // Applicant No44; selected in the past 2 times
            + "4\n44 65 ACCEPTED 44 63 ACCEPTED 44 69 IN_PROGRESS 44 62 WITHDRAWN\n"
            // Applicant No45; selected in the past: 2 times
            + "4\n45 69 IN_PROGRESS 45 63 ACCEPTED 45 65 ACCEPTED 45 43 ACCEPTED\n";
        addApplications(input);

        List<Long> courseIds = List.of(69L, 66L, 62L, 63L, 65L, 64L);
        Assertions
            .assertThat(((TimesSelectedStrategy) this.strategy)
                .recommendFromRepo(69L, courseIds, 3, 0.0))
            .containsExactlyInAnyOrder(new Recommendation(42L, 3.0),
                new Recommendation(45L, 2.0),
                new Recommendation(44L, 2.0));
    }

    @Test
    void testRecommendFromRepoNoMatchingCourseIds() {
        // Course with courseId 69 is a new course (no other courses with the same course code)
        // Number of applicants
        String input = TWO_LF
            // Applicant No42; selected in the past: 0 times
            + "4\n42 69 IN_PROGRESS 42 66 ACCEPTED 42 64 ACCEPTED 42 62 ACCEPTED\n"
            // Applicant No45; selected in the past: 0 times
            + "4\n45 69 IN_PROGRESS 45 63 ACCEPTED 45 65 ACCEPTED 45 43 ACCEPTED\n";
        addApplications(input);

        List<Long> courseIds = List.of(69L);
        Assertions
            .assertThat(((TimesSelectedStrategy) this.strategy)
                .recommendFromRepo(69L, courseIds, 3, 0.0))
            .isEmpty();
    }

    /**
     * Boundary testing for filtering.
     */

    @Test
    void testRecommendFromRepoOnPoint() {
        // Course with courseId 47 has the same course code as 34, 54, 23
        // Number of applicants
        String input = TWO_LF
            // Applicant No333; selected in the past: 3 times
            + "4\n333 47 IN_PROGRESS 333 34 ACCEPTED 333 54 ACCEPTED 333 23 ACCEPTED\n"
            // Applicant No112; selected in the past: 0 times
            + "3\n112 43 ACCEPTED 112 47 IN_PROGRESS 112 23 REJECTED\n";
        addApplications(input);

        List<Long> courseIds = List.of(47L, 34L, 54L, 23L);
        Assertions
            .assertThat(((TimesSelectedStrategy) this.strategy)
                .recommendFromRepo(47L, courseIds, 3, 3.0))
            .isEqualTo(List.of(new Recommendation(333L, 3.0)));
    }

    @Test
    void testRecommendFromRepoOffPoint() {
        // Course with courseId 69 has the same course code as 66, 62, 63, 65, 64
        // Number of applicants
        String input = TWO_LF
            // Applicant No33; selected in the past: 3 times
            + "4\n33 69 IN_PROGRESS 33 66 ACCEPTED 33 64 ACCEPTED 33 62 ACCEPTED\n"
            // Applicant No12; selected in the past: 1 time
            + "3\n12 64 ACCEPTED 12 69 IN_PROGRESS 12 66 REJECTED\n";
        addApplications(input);

        List<Long> courseIds = List.of(69L, 66L, 62L, 63L, 65L, 64L);
        Assertions
            .assertThat(((TimesSelectedStrategy) this.strategy)
                .recommendFromRepo(69L, courseIds, 4, 4.0))
            .isEmpty();
    }

    @Test
    void testRecommendFromRepoInPoint() {
        // Course with courseId 59 has the same course code as 56, 52, 53, 55, 54
        // Number of applicants
        String input = ONE_LF
            // Applicant No11; selected in the past: 4 times
            + "5\n11 59 IN_PROGRESS 11 56 ACCEPTED 11 54 ACCEPTED 11 53 ACCEPTED 11 55 ACCEPTED\n";
        addApplications(input);

        List<Long> courseIds = List.of(59L, 56L, 52L, 53L, 55L, 54L);
        Assertions
            .assertThat(((TimesSelectedStrategy) this.strategy)
                .recommendFromRepo(59L, courseIds, 1, 1.0))
            .isEqualTo(List.of(new Recommendation(11L, 4.0)));
    }

    @Test
    void testRecommendFromRepoOutPoint() {
        // Course with courseId 39 has the same course code as 36, 32, 33
        // Number of applicants
        String input = TWO_LF
            // Applicant No55; selected in the past: 1 time
            + "4\n55 39 IN_PROGRESS 55 36 WITHDRAWN 55 32 REJECTED 55 33 ACCEPTED\n"
            // Applicant No12; selected in the past: 0 times
            + "1\n12 69 IN_PROGRESS\n";
        addApplications(input);

        List<Long> courseIds = List.of(39L, 36L, 32L, 33L);
        Assertions
            .assertThat(((TimesSelectedStrategy) this.strategy)
                .recommendFromRepo(39L, courseIds, 6, 6.0))
            .isEmpty();
    }


    /**
     * A helper method to add applications to the repository.
     *
     * @param input the input string in the following format:
     *              N            (total number of users)
     *              n            (number of applications for the following user)
     *              userId courseId1 STATUS1 userId courseId2 STATUS2 ...
     *              ...
     *
     *              I needed to duplicate userId for each application, because PMD
     *              was complaining about DU anomalies since I was accessing the userId
     *              variable outside the for-loop.
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
                    Submission appl = new Submission(sc.nextLong(), sc.nextLong(), time);
                    appl.setStatus(SubmissionStatus.valueOf(sc.next()));
                    this.repo.save(appl);
                }
                sc.nextLine();
            }
        }
    }
}
