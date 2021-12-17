package nl.tudelft.sem.hiring.procedure.recommendation;

import java.util.List;

/**
 * The interface that represents a strategy for recommending a user.
 *   It is based on a certain metric (number of times selected, grade for the course,
 *   the number of hours worked as a TA etc
 */
public interface RecommendationStrategy {
    /**
     * Recommends at most the specified number of users who have applied for the specified course.
     *
     * @param courseId      the id of the course
     * @param number        the maximum number of recommendations for the course
     * @param minValue      the minimum value for the metric (used for filtering)
     * @return the list of recommendations for candidate TAs.
     *         The size of the list is at most 'number'
     */
    List<Recommendation> recommend(long courseId, int number, double minValue);
}
