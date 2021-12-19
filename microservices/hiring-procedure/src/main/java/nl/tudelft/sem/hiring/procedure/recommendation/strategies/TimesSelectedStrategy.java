package nl.tudelft.sem.hiring.procedure.recommendation.strategies;

import java.util.List;
import java.util.stream.Collectors;
import nl.tudelft.sem.hiring.procedure.recommendation.entities.Recommendation;
import nl.tudelft.sem.hiring.procedure.repositories.ApplicationRepository;
import nl.tudelft.sem.hiring.procedure.utils.GatewayConfig;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

/**
 * The class that implements RecommendationStrategy interface by recommending candidate TAs
 * based of the number of times they have been selected as a TA for
 * any edition of the given course.
 */
public class TimesSelectedStrategy implements RecommendationStrategy {

    private final transient GatewayConfig gatewayConfig;

    private final transient WebClient webClient;

    private final transient ApplicationRepository repo;

    /**
     * Instantiates a new TimesSelectedStrategy object.
     *
     * @param repo          the TA application repository
     * @param gatewayConfig the gateway configuration
     */
    public TimesSelectedStrategy(ApplicationRepository repo, GatewayConfig gatewayConfig) {
        this.repo = repo;
        this.webClient = WebClient.create();
        this.gatewayConfig = gatewayConfig;
    }

    /**
     * Recommends at most the specified number of candidate TAs who have applied
     * for a specified course.
     * It uses the strategy of the max times selected for any edition of the given course.
     *
     * @param courseId the id of the course to recommend for
     * @param amount   the maximum number of recommendations to return
     * @param minTimes the minimum number of times selected (used for filtering)
     * @return the list of recommendations for candidate TAs based on the number of
     *          times selected for a TA position for one course (wrapped in the mono).
     *          The size of the list is at most `amount`.
     */
    @Override
    public Mono<List<Recommendation>> recommend(long courseId, int amount, double minTimes) {

        if (this.repo.findAllApplicantsByCourseId(courseId).isEmpty()) {
            return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Could not find any applicants"));
        }
        return
            this.webClient
                .get()
                .uri(buildUriWithCourseId(gatewayConfig.getHost(), gatewayConfig.getPort(),
                    courseId, "api", "courses", "get-all-editions"))
                .exchange()
                .flatMap(response -> processMono(response,
                    body -> processMonoBody(body, courseId, amount, minTimes)));
    }

    /**
     * A helper method to process the received mono response body.
     *
     * @param body     the body from the received mono response
     * @param courseId the id of the course to recommend for
     * @param amount   the maximum number of recommendations to return
     * @param minTimes the minimum number of times selected (used for filtering)
     * @return the list of recommendations for candidate TAs based on the number of
     *          times selected for a TA position for one course (wrapped in the mono).
     *          The size of the list is at most 'amount'.
     */
    private Mono<List<Recommendation>> processMonoBody(String body, long courseId,
                                                       int amount, double minTimes) {
        try {
            List<Long> courseIds = convertJsonToLongList(body, "courseIds");
            return recommendationsToMono(recommendFromRepo(courseId, courseIds, amount, minTimes));
        } catch (Exception e) {
            // related to JSON processing (e.g. bad format, missing/incorrect fields)
            return Mono
                .error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "An error has occurred. Please try again later!"));
        }
    }

    /**
     * A helper method that queries the repository for recommendations.
     * I have decided to make this method public to make it testable,
     * and also because it can exist on its own provided that the list of course IDs with
     * the same course code as the given course is given.
     *
     * @param courseId  the id of the course to recommend for
     * @param courseIds ids of the courses with the same course code as the given course
     * @param amount    the maximum number of recommendations to return
     * @param minTimes  the minimum number of times selected (used for filtering)
     * @return the list of recommendations for candidate TAs based on the number of
     *          times selected for a TA position for one course.
     *          The size of the list is at most 'amount'.
     */
    public List<Recommendation> recommendFromRepo(long courseId, List<Long> courseIds,
                                                  int amount, double minTimes) {
        return this.repo
            .findTopByTimesSelected(courseId, courseIds,
                (long) minTimes, PageRequest.of(0, amount))
            .stream()
            .map(a -> new Recommendation((Long) a[0], ((Long) a[1]).doubleValue()))
            .collect(Collectors.toList());
    }
}
