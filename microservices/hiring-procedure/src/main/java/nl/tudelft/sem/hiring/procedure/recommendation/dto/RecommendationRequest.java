package nl.tudelft.sem.hiring.procedure.recommendation.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import nl.tudelft.sem.hiring.procedure.recommendation.strategies.StrategyType;

@Data
@NoArgsConstructor
public class RecommendationRequest {
    private long courseId;
    private int amount;
    private double minValue;
    private StrategyType strategy;
}
