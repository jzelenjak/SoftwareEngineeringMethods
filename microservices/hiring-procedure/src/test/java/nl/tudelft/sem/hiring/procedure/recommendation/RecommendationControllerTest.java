package nl.tudelft.sem.hiring.procedure.recommendation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import nl.tudelft.sem.hiring.procedure.entities.Application;
import nl.tudelft.sem.hiring.procedure.entities.ApplicationStatus;
import nl.tudelft.sem.hiring.procedure.recommendation.entities.Recommendation;
import nl.tudelft.sem.hiring.procedure.repositories.ApplicationRepository;
import nl.tudelft.sem.hiring.procedure.utils.GatewayConfig;
import nl.tudelft.sem.hiring.procedure.validation.AsyncRoleValidator;
import nl.tudelft.sem.jwt.JwtUtils;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class RecommendationControllerTest {
    @Autowired
    private transient ApplicationRepository repo;

    @MockBean
    private transient JwtUtils jwtUtils;

    @Mock
    private transient Jws<Claims> jwsMock;

    @MockBean
    private transient GatewayConfig gatewayConfig;

    @Autowired
    private transient MockMvc mockMvc;

    private transient MockWebServer mockWebServer;

    private final transient LocalDateTime time = LocalDateTime.now();

    private static final transient String RECOMMEND_URL = "/api/hiring-procedure"
                                    + "/recommendations/recommend";

    private void configureJwsMock(AsyncRoleValidator.Roles roleToReturn) {
        Mockito.when(jwtUtils.resolveToken(Mockito.any())).thenReturn("");
        Mockito.when(jwtUtils.validateAndParseClaims(Mockito.any())).thenReturn(jwsMock);
        Mockito.when(jwtUtils.getRole(Mockito.any())).thenReturn(roleToReturn.name());
    }

    @BeforeEach
    void startMockWebServer() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        HttpUrl url = mockWebServer.url("/");
        Mockito.when(gatewayConfig.getHost()).thenReturn(url.host());
        Mockito.when(gatewayConfig.getPort()).thenReturn(url.port());
    }

    @AfterEach
    void tearDownMockWebServer() throws IOException {
        mockWebServer.shutdown();
    }

    /**
     * Helper methods to make the tests for the 4 strategies more clean.
     */

    private String buildRequestBody(long courseId, int amount, double minValue, String strategy) {
        return new ObjectMapper().createObjectNode()
            .put("courseId", courseId).put("amount", amount)
            .put("minValue", minValue).put("strategy", strategy).toString();
    }

    private String buildExpectedResponseBody(long u1, double m1,
                                             long u2, double m2) throws Exception {
        List<Recommendation> expected =
            List.of(new Recommendation(u1, m1), new Recommendation(u2, m2));
        return new ObjectMapper().writeValueAsString(expected);
    }

    private String buildResponseFromCourses(List<Long> courseIds) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.createObjectNode().set("courseIds", mapper.valueToTree(courseIds)).toString();
    }

    private void assertRequestAndResponse(String content, String expected) throws Exception {
        MvcResult mvcResult = mockMvc.perform(post(RECOMMEND_URL)
                .header(HttpHeaders.AUTHORIZATION, "someValidJWT")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content))
            .andReturn();
        mockMvc.perform(asyncDispatch(mvcResult)).andExpect(status().isOk())
            .andExpect(content().string(expected));
    }

    private void assertRecordedRequest(String method) throws InterruptedException {
        RecordedRequest request = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        Assertions.assertThat(request).isNotNull();
        Assertions.assertThat(request.getMethod()).isEqualTo(method);
    }

    @Test
    void testRecommendTotalTimesSelectedStrategy() throws Exception {
        // Applicant No42: 3 times; No43: 1 time; No44: 2 times; No45: 4 times; No46: 0 times
        String input = "5\n"
            + "4\n42 777 IN_PROGRESS 42 66 ACCEPTED 42 68 ACCEPTED 42 61 ACCEPTED\n"
            + "3\n43 56 ACCEPTED 43 777 IN_PROGRESS 43 666 REJECTED\n"
            + "4\n44 39 ACCEPTED 44 49 ACCEPTED 44 777 IN_PROGRESS 44 62 WITHDRAWN\n"
            + "4\n45 21 ACCEPTED 45 76 ACCEPTED 45 23 ACCEPTED 45 43 ACCEPTED\n"
            + "1\n46 777 IN_PROGRESS\n";
        addApplications(input);
        configureJwsMock(AsyncRoleValidator.Roles.ADMIN);

        String jsonRequest = buildRequestBody(777L, 5, 2.0, "TOTAL_TIMES_SELECTED");
        String expectedResponse = buildExpectedResponseBody(42L, 3, 44L, 2);
        assertRequestAndResponse(jsonRequest, expectedResponse);
    }

    @Test
    void testRecommendTimesSelectedStrategy() throws Exception {
        // Course with courseId 61 has the same course code as 62, 63, 64,
        // Applicant No42: 2 times; No43: 0 times; No44: 1 times; No46: 0 times
        String input = "5\n"
            + "4\n42 61 IN_PROGRESS 42 68 ACCEPTED 42 64 ACCEPTED 42 62 ACCEPTED\n"
            + "3\n43 62 REJECTED 43 61 IN_PROGRESS 43 63 REJECTED\n"
            + "4\n44 64 ACCEPTED 44 60 ACCEPTED 44 61 IN_PROGRESS 44 68 WITHDRAWN\n"
            + "4\n45 62 ACCEPTED 45 63 ACCEPTED 45 64 ACCEPTED 45 68 ACCEPTED\n"
            + "1\n46 61 IN_PROGRESS\n";
        addApplications(input);
        configureJwsMock(AsyncRoleValidator.Roles.ADMIN);

        mockWebServer.enqueue(new MockResponse().setResponseCode(200)
            .setBody(buildResponseFromCourses(List.of(61L, 62L, 63L, 64L))));

        String jsonRequest = buildRequestBody(61L, 3, 1.0, "TIMES_SELECTED");
        String expectedResponse = buildExpectedResponseBody(42L, 2, 44L, 1);
        assertRequestAndResponse(jsonRequest, expectedResponse);
        assertRecordedRequest("GET");
    }

    @Test
    void testGradeStrategy() throws Exception {
        // Applicants No88, No81, No86; Course with courseId 23
        this.repo.save(new Application(88, 23L, time));
        this.repo.save(new Application(81, 23L, time));
        this.repo.save(new Application(86, 23L, time));

        String json = new ObjectMapper().writeValueAsString(Map.of(88L, 9.1, 86L, 8.2));
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(json));

        configureJwsMock(AsyncRoleValidator.Roles.ADMIN);

        String jsonRequest = buildRequestBody(23L, 3, 8.0, "GRADE");
        String expectedResponse = buildExpectedResponseBody(88L, 9.1, 86L, 8.2);
        assertRequestAndResponse(jsonRequest, expectedResponse);
        assertRecordedRequest("POST");
    }

    @Test
    void testHoursStrategy() throws Exception {
        // Applicants No36,37,38; Course with courseId 6666 (same code as 6665)
        this.repo.save(new Application(36L, 6666L, time));
        this.repo.save(new Application(37L, 6666L, time));
        this.repo.save(new Application(38L, 6666L, time));

        configureJwsMock(AsyncRoleValidator.Roles.LECTURER);

        ObjectMapper mapper = new ObjectMapper();
        String jsonFromCourses = mapper.createObjectNode()
            .set("courseIds", mapper.valueToTree(List.of(6666L, 6665L))).toString();
        String jsonFromHours = mapper
            .writeValueAsString(Map.of(36L, 101.0, 38L, 135.0));

        mockWebServer.setDispatcher(dispatcher(jsonFromCourses, jsonFromHours));

        String jsonRequest = buildRequestBody(6666L, 8, 100.0, "HOURS");
        String expectedResponse = buildExpectedResponseBody(38L, 135.0, 36L, 101.0);
        assertRequestAndResponse(jsonRequest, expectedResponse);
        assertRecordedRequest("GET");
        assertRecordedRequest("POST");
    }

    /**
     * Tests for edge cases (exceptions, unauthorized).
     */
    @Test
    void testRecommendTaMustBeForbidden() throws Exception {
        configureJwsMock(AsyncRoleValidator.Roles.TA);

        MvcResult mvcResult = this.mockMvc.perform(post(RECOMMEND_URL)
            .header(HttpHeaders.AUTHORIZATION, "jwt")).andReturn();
        mockMvc.perform(asyncDispatch(mvcResult)).andExpect(status().isForbidden());
    }

    @Test
    void testRecommendStudentMustBeForbidden() throws Exception {
        configureJwsMock(AsyncRoleValidator.Roles.STUDENT);

        MvcResult mvcResult = this.mockMvc.perform(post(RECOMMEND_URL)
            .header(HttpHeaders.AUTHORIZATION, "jwt")).andReturn();
        mockMvc.perform(asyncDispatch(mvcResult)).andExpect(status().isForbidden());
    }

    @Test
    void testRecommendNotNumberInJson() throws Exception {
        configureJwsMock(AsyncRoleValidator.Roles.LECTURER);
        String json = new ObjectMapper().createObjectNode()
            .put("courseId", "ImpostorCourseId")
            .toString();

        MvcResult mvcResult = mockMvc.perform(post(RECOMMEND_URL)
            .header(HttpHeaders.AUTHORIZATION, "jwt")
            .contentType(MediaType.APPLICATION_JSON).content(json)).andReturn();
        mockMvc.perform(asyncDispatch(mvcResult)).andExpect(status().isBadRequest());
    }

    @Test
    void testRecommendBadJson() throws Exception {
        configureJwsMock(AsyncRoleValidator.Roles.LECTURER);

        MvcResult mvcResult = mockMvc.perform(post(RECOMMEND_URL)
            .header(HttpHeaders.AUTHORIZATION, "JWT")
            .contentType(MediaType.APPLICATION_JSON).content("fasfasdf")).andReturn();
        mockMvc.perform(asyncDispatch(mvcResult)).andExpect(status().isBadRequest());
    }

    @Test
    void testRecommendMissingFieldInJson() throws Exception {
        configureJwsMock(AsyncRoleValidator.Roles.LECTURER);
        String json = new ObjectMapper().createObjectNode()
            .put("course", 4242442L)
            .toString();

        MvcResult mvcResult = this.mockMvc.perform(post(RECOMMEND_URL)
            .header(HttpHeaders.AUTHORIZATION, "JWT")
            .contentType(MediaType.APPLICATION_JSON).content(json)).andReturn();
        mockMvc.perform(asyncDispatch(mvcResult)).andExpect(status().isBadRequest());
    }

    @Test
    void testRecommendInvalidStrategy() throws Exception {
        configureJwsMock(AsyncRoleValidator.Roles.LECTURER);

        MvcResult mvcResult = this.mockMvc.perform(post(RECOMMEND_URL)
            .header(HttpHeaders.AUTHORIZATION, "someValidJWT")
            .contentType(MediaType.APPLICATION_JSON)
            .content(buildRequestBody(4242442L, 5, 42.69, "GREEDY")))
            .andReturn();
        mockMvc.perform(asyncDispatch(mvcResult)).andExpect(status().isBadRequest());
    }

    /**
     * A helper method that is used to handle the sequence of responses.
     * It is used when testing HourStrategy since it requires 2 requests to be made
     *  (Courses and Hour Management microservice)
     *
     * @param jsonFromCourses json response body from Courses microservice
     * @param jsonFromHours   json response body from Hour Management microservice
     * @return the created dispatcher that will receive a request, check its URL's
     *          endpoint and return corresponding response.
     */
    private Dispatcher dispatcher(String jsonFromCourses, String jsonFromHours) {
        return new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                if (request.getPath().contains("/api/courses/get-all-editions")) {
                    return new MockResponse().setResponseCode(200).setBody(jsonFromCourses);
                }
                if (request.getPath()
                        .contains("/api/hour-management/statistics/total-user-hours")) {
                    return new MockResponse().setResponseCode(200).setBody(jsonFromHours);
                }
                return new MockResponse().setResponseCode(404);
            }
        };
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
                    Application appl = new Application(sc.nextLong(), sc.nextLong(), time);
                    appl.setStatus(ApplicationStatus.valueOf(sc.next()));
                    this.repo.save(appl);
                }
                sc.nextLine();
            }
        }
    }
}
