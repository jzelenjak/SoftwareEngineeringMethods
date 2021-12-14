package nl.tudelft.sem.hiring.procedure.recommendation;

import java.util.List;

public class ApplicationRecommender {
    private final transient RecommendationStrategy strategy;

    /**
     * Instantiates a new Application recommender.
     *
     * @param strategy the strategy we want to use.
     */
    public ApplicationRecommender(RecommendationStrategy strategy) {
        this.strategy = strategy;
    }

    public List<Recommendation> recommend(long courseId, int number) {
        return this.strategy.recommend(courseId, number);
    }
}
