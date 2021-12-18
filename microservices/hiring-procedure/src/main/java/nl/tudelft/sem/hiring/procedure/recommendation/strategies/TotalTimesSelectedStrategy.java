package nl.tudelft.sem.hiring.procedure.recommendation.strategies;

import java.util.List;
import java.util.stream.Collectors;
import nl.tudelft.sem.hiring.procedure.recommendation.entities.Recommendation;
import nl.tudelft.sem.hiring.procedure.repositories.ApplicationRepository;
import org.springframework.data.domain.PageRequest;
import reactor.core.publisher.Mono;

/**
 * The class that implements RecommendationStrategy interface by recommending candidate TAs
 *      based of the total number of times they have been selected as a TA for any course.
 */
public class TotalTimesSelectedStrategy implements RecommendationStrategy {
    private final transient ApplicationRepository repo;

    /**
     * Instantiates a new TotalTimesSelectedStrategy object.
     *
     * @param repo the TA application repository
     */
    public TotalTimesSelectedStrategy(ApplicationRepository repo) {
        this.repo = repo;
    }

    /**
     * Recommends at most the specified number of candidate TAs who have applied
     *   for a specified course.
     *   It uses the strategy of the max total times selected for any course.
     *
     * @param courseId      the id of the course
     * @param amount        the maximum number of recommendations to return
     * @param minTimes      the minimum number of times selected (used for filtering)
     * @return the list of recommendations for candidate TAs based on the number of
     *         times selected for a TA position (wrapped in the mono).
     *         The size of the list is at most `amount`.
     */
    @Override
    public Mono<List<Recommendation>> recommend(long courseId, int amount, double minTimes) {
        return Mono.just(this.repo
                .findTopByTotalTimesSelected(courseId, (long) minTimes, PageRequest.of(0, amount))
                .stream()
                .map(t -> new Recommendation((Long) t[0], ((Long) t[1]).doubleValue()))
                .collect(Collectors.toList()));
    }
}
