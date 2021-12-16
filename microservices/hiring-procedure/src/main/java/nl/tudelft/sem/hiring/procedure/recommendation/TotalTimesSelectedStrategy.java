package nl.tudelft.sem.hiring.procedure.recommendation;

import java.util.List;
import java.util.stream.Collectors;
import nl.tudelft.sem.hiring.procedure.repositories.ApplicationRepository;

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
     * @param number        the maximum number of recommendations to return
     * @return the list of recommendations for candidate TAs based on the number of
     *         times selected for a TA position. The size of the list is at most 'number'.
     */
    public List<Recommendation> recommend(long courseId, int number) {
        return this.repo.findTopByTotalTimesSelected(courseId)
                .stream()
                .limit(number)
                .map(a -> new Recommendation((Long) a[0], ((Long) a[1]).doubleValue()))
                .collect(Collectors.toList());
    }
}
