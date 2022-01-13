package nl.tudelft.sem.hiring.procedure.recommendation.factory;

import nl.tudelft.sem.hiring.procedure.recommendation.strategies.GradeStrategy;
import nl.tudelft.sem.hiring.procedure.recommendation.strategies.HoursStrategy;
import nl.tudelft.sem.hiring.procedure.recommendation.strategies.RecommendationStrategy;
import nl.tudelft.sem.hiring.procedure.recommendation.strategies.StrategyType;
import nl.tudelft.sem.hiring.procedure.recommendation.strategies.TimesSelectedStrategy;
import nl.tudelft.sem.hiring.procedure.recommendation.strategies.TotalTimesSelectedStrategy;
import nl.tudelft.sem.hiring.procedure.repositories.SubmissionRepository;
import nl.tudelft.sem.hiring.procedure.utils.GatewayConfig;

/**
 * A factory class that creates different recommendation strategies from the given parameters.
 */
public class StrategyFactory {

    private StrategyFactory() {

    }

    /**
     * Creates a recommendation strategy from the given parameters.
     *
     * @param repo              the submission repository
     * @param config            the gateway config
     * @param jwt               the jwt token of the requester
     * @param strategyType      the type of the recommendation strategy
     * @return recommendation strategy object
     */
    public static RecommendationStrategy create(SubmissionRepository repo, GatewayConfig config,
                                                String jwt, StrategyType strategyType) {
        switch (strategyType) {
            case TOTAL_TIMES_SELECTED:
                return new TotalTimesSelectedStrategy(repo, config, jwt);
            case TIMES_SELECTED:
                return new TimesSelectedStrategy(repo, config, jwt);
            case GRADE:
                return new GradeStrategy(repo, config, jwt);
            //HOURS
            default:
                return new HoursStrategy(repo, config, jwt);
        }
    }
}
