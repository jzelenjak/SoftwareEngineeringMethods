package nl.tudelft.sem.hiring.procedure.recommendation.strategies;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import nl.tudelft.sem.hiring.procedure.recommendation.entities.Recommendation;
import nl.tudelft.sem.hiring.procedure.repositories.ApplicationRepository;
import nl.tudelft.sem.hiring.procedure.utils.GatewayConfig;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

/**
 * The class that implements RecommendationStrategy interface by recommending candidate TAs
 *      based of the total number of hours they have been working as a TA for a given course.
 */
public class HoursStrategy implements RecommendationStrategy {

    private final transient GatewayConfig gatewayConfig;

    private final transient WebClient webClient;

    private final transient ApplicationRepository repo;

    /**
     * Instantiates a new GradeStrategy object.
     *
     * @param repo the TA application repository
     * @param gatewayConfig the gateway configuration
     */
    public HoursStrategy(ApplicationRepository repo, GatewayConfig gatewayConfig) {
        this.repo = repo;
        this.webClient = WebClient.create();
        this.gatewayConfig = gatewayConfig;
    }

    /**
     * Recommends at most the specified number of candidate TAs who have applied
     *   for a specified course.
     *   It uses the strategy of the max total times selected for any course.
     *
     * @param courseId      the id of the course
     * @param amount        the maximum number of recommendations to return
     * @param minValue      the minimum value for the metric (used for filtering)
     * @return the list of recommendations for candidate TAs based on the number of
     *         times selected for a TA position (wrapped in the mono).
     *         The size of the list is at most 'amount'.
     */
    @Override
    public Mono<List<Recommendation>> recommend(long courseId, int amount, double minValue) {
        List<Long> applicants = repo.findAllApplicants(courseId);

        if (applicants.isEmpty()) {
            return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "No applicants found"));
        }

        return this.webClient
                .get()
                .uri(buildUriWithCourseId(gatewayConfig.getHost(), gatewayConfig.getPort(),
                        courseId, "api", "courses", "get-all-editions"))
                .exchange()
                .flatMap(response -> processMonoFromCourses(response,
                                courseId, amount, minValue, applicants));
    }

    private Mono<List<Recommendation>> processMonoFromCourses(ClientResponse response,
                                                       long courseId, int number,
                                                       double minValue, List<Long> userIds) {
        if (response.statusCode().isError()) {
            return Mono
                    .error(new ResponseStatusException(response.statusCode(),
                            "Could not find any courses"));
        }
        return response
                .bodyToMono(String.class)
                .flatMap(res -> processMonoBodyFromCourses(res, number, minValue, userIds));
    }

    protected Mono<List<Recommendation>> processMonoBodyFromCourses(String body,
                                                int amount, double minValue, List<Long> userIds) {
        try {
            List<Long> courseIds = convertJsonToLongList(body, "courseIds");

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode objectNode = mapper.createObjectNode();
            objectNode.put("minHours", minValue);
            objectNode.put("amount", amount);
            objectNode.set("userIds", mapper.valueToTree(userIds));
            objectNode.set("courseIds", mapper.valueToTree(courseIds));

            return this.webClient
                    .post()
                    .uri(buildUri(gatewayConfig.getHost(), gatewayConfig.getPort(),
                            "api", "hour-management", "statistics", "total-user-hours"))
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(objectNode.toString(), String.class)
                    .exchange()
                    .flatMap(this::processMonoFromHourManagement);
        } catch (JsonProcessingException e) {
            return Mono
                    .error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                            "An error has occurred. Please try again later!"));
        }
    }

    private Mono<List<Recommendation>> processMonoFromHourManagement(ClientResponse response) {
        if (response.statusCode().isError()) {
            return Mono
                    .error(new ResponseStatusException(response.statusCode(),
                            "Could not make any recommendations"));
        }
        return response
                .bodyToMono(String.class)
                .flatMap(this::processMonoBodyFromHourManagement);
    }

    protected Mono<List<Recommendation>> processMonoBodyFromHourManagement(String body) {
        try {
            return Mono.just(convertJsonToRecommendationList(body));
        } catch (JsonProcessingException e) {
            return Mono
                    .error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                            "An error has occurred. Please try again later!"));
        }
    }
}
