package nl.tudelft.sem.hiring.procedure.recommendation.strategies;

import java.util.List;
import java.util.stream.Collectors;
import nl.tudelft.sem.hiring.procedure.recommendation.entities.Recommendation;
import nl.tudelft.sem.hiring.procedure.repositories.SubmissionRepository;
import nl.tudelft.sem.hiring.procedure.utils.GatewayConfig;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

/**
 * The class that extends BaseStrategy class and implements RecommendationStrategy interface by
 *  recommending candidate TAs based of the total number of times they have been selected as
 *  a TA for any course.
 */
public class TotalTimesSelectedStrategy extends RecommendationStrategyBase {

    /**
     * Instantiates a new TotalTimesSelectedStrategy object.
     *
     * @param repo the TA application repository
     * @param gatewayConfig the gateway configuration.
     * @param authorization the authorization token of the caller.
     */
    public TotalTimesSelectedStrategy(SubmissionRepository repo, GatewayConfig gatewayConfig,
                                      String authorization) {
        super(repo, gatewayConfig, authorization);
    }

    /**
     * Recommends at most the specified number of candidate TAs who have applied
     *  for a specified course. It uses the strategy of the max total times selected for any course.
     *
     * @param courseId the id of the course
     * @param amount   the maximum number of recommendations to return
     * @param minTimes the minimum number of times selected (used for filtering)
     * @return the list of recommendations for candidate TAs based on the number of
     *          times selected for a TA position (wrapped in the mono).
     *          The size of the list is at most `amount`.
     */
    @Override
    public Mono<List<Recommendation>> recommend(long courseId, int amount, double minTimes) {
        List<Recommendation> recommendations = this.repo
            .findTopByTotalTimesSelected(courseId, (long) minTimes, PageRequest.of(0, amount))
            .stream()
            .map(t -> new Recommendation((Long) t[0], ((Long) t[1]).doubleValue()))
            .collect(Collectors.toList());
        if (recommendations.isEmpty()) {
            return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Could not find any recommendations"));
        }
        return Mono.just(recommendations);
    }
}
