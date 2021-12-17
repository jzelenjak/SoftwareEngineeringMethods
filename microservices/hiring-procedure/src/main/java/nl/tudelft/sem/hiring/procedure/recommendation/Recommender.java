package nl.tudelft.sem.hiring.procedure.recommendation;

import java.util.List;
import javax.validation.constraints.NotNull;
import reactor.core.publisher.Mono;

/**
 * A class is responsible for recommending candidate TAs for a course based on different strategies.
 */
public class Recommender {
    @NotNull
    private final transient RecommendationStrategy strategy;

    /**
     * Instantiates a new Recommender object.
     *
     * @param strategy the strategy that is used for the recommendation.
     */
    public Recommender(RecommendationStrategy strategy) {
        this.strategy = strategy;
    }

    /**
     * Recommend at most the specified number of candidate TAs for the given course.
     *
     * @param courseId  the id of the course (not the course code)
     * @param number    the maximum number of recommendations to return
     * @param minValue  the minimum value for the metric (used for filtering)
     * @return a list of recommendations for the specified course.
     *          The list is at most size 'number'
     */
    public Mono<List<Recommendation>> recommend(long courseId, int number, double minValue) {
        return this.strategy.recommend(courseId, number, minValue);
    }
}
