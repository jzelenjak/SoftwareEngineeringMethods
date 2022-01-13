package nl.tudelft.sem.hiring.procedure.recommendation.strategies;

import java.util.List;
import nl.tudelft.sem.hiring.procedure.recommendation.entities.Recommendation;
import reactor.core.publisher.Mono;

/**
 * The interface that provides functionality for recommending a user. The strategies that
 * implement this interface are based on a certain metric (total times selected, times selected
 * for a given course, the highest grade or the most hours spent working on the given course).
 */
public interface Recommender {

    /**
     * Recommends at most the specified number of users who have applied for the specified course.
     *
     * @param courseId the id of the course
     * @param amount   the maximum number of recommendations for the course
     * @param minValue the minimum value for the metric (used for filtering)
     * @return the list of recommendations for candidate TAs.
     *          The size of the list is at most `amount`
     */
    Mono<List<Recommendation>> recommend(long courseId, int amount, double minValue);
}
