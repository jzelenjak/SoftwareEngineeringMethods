package nl.tudelft.sem.hiring.procedure.recommendation.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import nl.tudelft.sem.hiring.procedure.recommendation.entities.Recommendation;
import nl.tudelft.sem.hiring.procedure.recommendation.service.Recommender;
import nl.tudelft.sem.hiring.procedure.recommendation.strategies.GradeStrategy;
import nl.tudelft.sem.hiring.procedure.recommendation.strategies.HoursStrategy;
import nl.tudelft.sem.hiring.procedure.recommendation.strategies.RecommendationStrategy;
import nl.tudelft.sem.hiring.procedure.recommendation.strategies.StrategyType;
import nl.tudelft.sem.hiring.procedure.recommendation.strategies.TimesSelectedStrategy;
import nl.tudelft.sem.hiring.procedure.recommendation.strategies.TotalTimesSelectedStrategy;
import nl.tudelft.sem.hiring.procedure.repositories.SubmissionRepository;
import nl.tudelft.sem.hiring.procedure.utils.GatewayConfig;
import nl.tudelft.sem.hiring.procedure.validation.AsyncAuthValidator;
import nl.tudelft.sem.hiring.procedure.validation.AsyncRoleValidator;
import nl.tudelft.sem.hiring.procedure.validation.AsyncValidator;
import nl.tudelft.sem.jwt.JwtUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

/**
 * Controller class for recommendations that provides the API.
 */
@RestController
@RequestMapping("/api/hiring-procedure/recommendations")
public class RecommendationController {

    private final transient GatewayConfig gatewayConfig;

    private final transient JwtUtils jwtUtils;

    private final transient SubmissionRepository submissionRepository;

    /**
     * Instantiates a new RecommendationController object.
     *
     * @param repo          the application repository
     * @param gatewayConfig the gateway configuration
     */
    public RecommendationController(SubmissionRepository repo,
                                    GatewayConfig gatewayConfig, JwtUtils jwtUtils) {
        this.submissionRepository = repo;
        this.gatewayConfig = gatewayConfig;
        this.jwtUtils = jwtUtils;
    }

    /**
     * Gets at most `amount` of candidate TA recommendations for a given course.
     *
     * @param body    HTTP request body (not required, because then
     *                a more customized exception can be sent)
     * @param headers HTTP request headers
     * @return a list of recommendations for the specified course.
     */
    @PostMapping("/recommend")
    public Mono<List<Recommendation>> recommend(@RequestBody(required = false) String body,
                                                @RequestHeader HttpHeaders headers) {
        AsyncValidator head = AsyncValidator.Builder
                .newBuilder()
                .addValidators(new AsyncAuthValidator(jwtUtils), new AsyncRoleValidator(jwtUtils,
                        Set.of(AsyncRoleValidator.Roles.LECTURER, AsyncRoleValidator.Roles.ADMIN)))
                .build();
        return head.validate(headers, body).flatMap(value ->
                parseBodyAndRecommend(body, headers.getFirst(HttpHeaders.AUTHORIZATION)));
    }


    /**
     * A helper method that selects the specified strategy and makes recommendations.
     *
     * @param courseId      the id of the course
     * @param amount        the maximum number of recommendations to return
     * @param minValue      the minimum value for the metric (used for filtering)
     * @param strategy      the metric to base recommendations on
     *                      (total times selected, times selected for a given course,
     *                      the highest grade or the most hours spent working on the given course)
     * @param authorization the authorization token of the caller
     * @return the list of recommendations for candidate TAs based on the specified metric
     *         (wrapped in the mono). The size of the list is at most `amount`.
     */
    private Mono<List<Recommendation>> recommend(long courseId, int amount,
                                                 double minValue, StrategyType strategy,
                                                 String authorization) {
        switch (strategy) {
            case TOTAL_TIMES_SELECTED:
                RecommendationStrategy strategy1 =
                        new TotalTimesSelectedStrategy(submissionRepository);
                return new Recommender(strategy1).recommend(courseId, amount, minValue);
            case TIMES_SELECTED:
                RecommendationStrategy strategy2 =
                        new TimesSelectedStrategy(submissionRepository, gatewayConfig,
                                authorization);
                return new Recommender(strategy2).recommend(courseId, amount, minValue);
            case GRADE:
                RecommendationStrategy strategy3 =
                        new GradeStrategy(submissionRepository, gatewayConfig, authorization);
                return new Recommender(strategy3).recommend(courseId, amount, minValue);
            //HOURS
            default:
                RecommendationStrategy strategy4 =
                        new HoursStrategy(submissionRepository, gatewayConfig, authorization);
                return new Recommender(strategy4).recommend(courseId, amount, minValue);
        }
    }

    /**
     * A helper method to parse the request JSON body and recommend candidate TAs.
     *
     * @param body          HTTP request body
     * @param authorization the authorization token of the caller
     * @return the list of recommendations for candidate TAs based on the specified metric
     *         (wrapped in the mono).
     */
    private Mono<List<Recommendation>> parseBodyAndRecommend(String body, String authorization) {
        try {
            JsonNode node = new ObjectMapper().readTree(body);
            long courseId = Long.parseLong(node.get("courseId").asText());
            int amount = Integer.parseInt(node.get("amount").asText());
            double minValue = Double.parseDouble(node.get("minValue").asText());
            StrategyType strategy = StrategyType
                    .valueOf(node.get("strategy").asText().toUpperCase(Locale.ROOT));

            return recommend(courseId, amount, minValue, strategy, authorization);
        } catch (JsonProcessingException e) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid request body format"));
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot parse the number provided");
        } catch (IllegalArgumentException e) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "The specified strategy does not exist"));
        } catch (Exception e) {
            // Will be NullPointerException. Since, PMD complains about catching NPE,
            //  a general exception put here
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Missing fields in the body"));
        }
    }
}
