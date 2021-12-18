package nl.tudelft.sem.hiring.procedure.recommendation.strategies;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import java.util.stream.Collectors;
import nl.tudelft.sem.hiring.procedure.recommendation.entities.Recommendation;
import nl.tudelft.sem.hiring.procedure.repositories.ApplicationRepository;
import nl.tudelft.sem.hiring.procedure.utils.GatewayConfig;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

/**
 * The class that implements RecommendationStrategy interface by recommending candidate TAs
 *  based of the number of times they have been selected as a TA for
 *  any edition of the given course.
 */
public class TimesSelectedStrategy implements RecommendationStrategy {

    private final transient GatewayConfig gatewayConfig;

    private final transient WebClient webClient;

    private final transient ApplicationRepository repo;

    /**
     * Instantiates a new TimesSelectedStrategy object.
     *
     * @param repo the TA application repository
     * @param gatewayConfig the gateway configuration
     */
    public TimesSelectedStrategy(ApplicationRepository repo, GatewayConfig gatewayConfig) {
        this.repo = repo;
        this.webClient = WebClient.create();
        this.gatewayConfig = gatewayConfig;
    }

    /**
     * Recommends at most the specified number of candidate TAs who have applied
     *   for a specified course.
     *   It uses the strategy of the max times selected for any edition of the given course.
     *
     * @param courseId      the id of the course
     * @param amount        the maximum number of recommendations to return
     * @param minValue      the minimum value for the metric (used for filtering)
     * @return the list of recommendations for candidate TAs based on the number of
     *         times selected for a TA position for one course (wrapped in the mono).
     *         The size of the list is at most 'amount'.
     */
    @Override
    public Mono<List<Recommendation>> recommend(long courseId, int amount, double minValue) {
        return
           this.webClient
            .get()
            .uri(buildUriWithCourseId(gatewayConfig.getHost(), gatewayConfig.getPort(), courseId,
                    "api", "courses", "get-all-editions"))
            .exchange()
            .flatMap(response -> processMono(response, courseId, amount, minValue));
    }

    /**
     * A helper method to process the received mono response.
     *
     * @param response   the received mono response
     * @param courseId   the id of the course
     * @param amount     the maximum number of recommendations to return
     * @param minValue   the minimum value for the metric (used for filtering)
     * @return the list of recommendations for candidate TAs based on the number of
     *         times selected for a TA position for one course (wrapped in the mono).
     *         The size of the list is at most 'amount'.
     */
    private Mono<List<Recommendation>> processMono(ClientResponse response, long courseId,
                                                        int amount, double minValue) {
        if (response.statusCode().isError()) {
            return Mono.error(new ResponseStatusException(response.statusCode(),
                            "Could not find any courses"));
        }
        return response
                .bodyToMono(String.class)
                .flatMap(body -> processMonoBody(body, courseId, amount, minValue));
    }

    /**
     * A helper method to process the received mono response body.
     *
     * @param body      the body from the received mono response
     * @param courseId  the id of the course
     * @param amount    the maximum number of recommendations to return
     * @param minValue  the minimum value for the metric (used for filtering)
     * @return the list of recommendations for candidate TAs based on the number of
     *         times selected for a TA position for one course (wrapped in the mono).
     *         The size of the list is at most 'amount'.
     */
    protected Mono<List<Recommendation>> processMonoBody(String body, long courseId,
                                                   int amount, double minValue) {
        try {
            List<Long> courseIds = convertJsonToLongList(body, "courseIds");
            return Mono
                    .just(this.repo
                            .findTopByTimesSelected(courseId, courseIds, (long) minValue)
                            .stream()
                            .limit(amount)
                            .map(a -> new Recommendation((Long) a[0], ((Long) a[1]).doubleValue()))
                            .collect(Collectors.toList())
                    );
        } catch (JsonProcessingException e) {
            return Mono
                    .error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                            "An error has occurred. Please try again later!"));
        }
    }

}
