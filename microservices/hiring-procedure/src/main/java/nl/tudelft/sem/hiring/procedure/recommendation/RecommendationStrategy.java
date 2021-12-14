package nl.tudelft.sem.hiring.procedure.recommendation;

import java.util.List;

public interface RecommendationStrategy {
    List<Recommendation> recommend(long courseId, int number);
}
