package nl.tudelft.sem.hiring.procedure.recommendation.controller;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import nl.tudelft.sem.hiring.procedure.recommendation.entities.Recommendation;
import nl.tudelft.sem.hiring.procedure.recommendation.entities.Recommender;
import nl.tudelft.sem.hiring.procedure.recommendation.strategies.GradeStrategy;
import nl.tudelft.sem.hiring.procedure.recommendation.strategies.HoursStrategy;
import nl.tudelft.sem.hiring.procedure.recommendation.strategies.RecommendationStrategy;
import nl.tudelft.sem.hiring.procedure.recommendation.strategies.TimesSelectedStrategy;
import nl.tudelft.sem.hiring.procedure.recommendation.strategies.TotalTimesSelectedStrategy;
import nl.tudelft.sem.hiring.procedure.repositories.ApplicationRepository;
import nl.tudelft.sem.hiring.procedure.utils.GatewayConfig;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

/**
 * Controller class for recommendations that provides the API.
 */
@RestController
@RequestMapping("/api/hiring-procedure/recommend")
public class RecommendationController {

    private final transient GatewayConfig gatewayConfig;

    private final transient ApplicationRepository applicationRepository;

    /**
     * Instantiates a new RecommendationController object.
     *
     * @param repo              the application repository
     * @param gatewayConfig     the gateway configuration
     */
    public RecommendationController(ApplicationRepository repo, GatewayConfig gatewayConfig) {
        this.applicationRepository = repo;
        this.gatewayConfig = gatewayConfig;
    }

    /**
     * Gets at most `amount` of candidate TA recommendations for a given course.
     *
     * @param req   HTTP request
     * @return a list of recommendations for the specified course. The list is at most size 'number'
     * @throws IOException if something goes wrong with servlets
     */
    @PostMapping("/")
    public Mono<List<Recommendation>> recommend(HttpServletRequest req) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(req.getInputStream());

        long courseId = node.get("courseId").asLong();
        int amount = node.get("amount").asInt();
        double minValue = node.get("minValue").asDouble();
        String metric = node.get("metric").asText().toUpperCase(Locale.ROOT);

        return recommend(courseId, amount, minValue, metric);
    }

    /**
     * A helper method that selects the specified strategy and makes recommendations.
     *
     * @param courseId   the id of the course
     * @param amount     the maximum number of recommendations to return
     * @param minValue   the minimum value for the metric (used for filtering)
     * @param metric     the metric to base recommendations on
     *                   (total times selected, times selected for a given course,
     *                   the highest grade or the most hours spent working on the given course)
     * @return the list of recommendations for candidate TAs based on the the specified metric
     *          (wrapped in the mono). The size of the list is at most `amount`.
     */
    private Mono<List<Recommendation>> recommend(long courseId, int amount,
                                                 double minValue, String metric) {
        switch (metric) {
            case "TOTAL_TIMES_SELECTED":
                RecommendationStrategy strategy1 =
                        new TotalTimesSelectedStrategy(applicationRepository);
                return new Recommender(strategy1).recommend(courseId, amount, minValue);
            case "TIMES_SELECTED":
                RecommendationStrategy strategy2 =
                        new TimesSelectedStrategy(applicationRepository, gatewayConfig);
                return new Recommender(strategy2).recommend(courseId, amount, minValue);
            case "GRADE":
                RecommendationStrategy strategy3 =
                        new GradeStrategy(applicationRepository, gatewayConfig);
                return new Recommender(strategy3).recommend(courseId, amount, minValue);
            case "HOURS":
                RecommendationStrategy strategy4 =
                        new HoursStrategy(applicationRepository, gatewayConfig);
                return new Recommender(strategy4).recommend(courseId, amount, minValue);
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Unsupported recommendation metric");
        }
    }

    /**
     * Exception handler for invalid JSONs.
     */
    @ExceptionHandler(JsonParseException.class)
    private void invalidJson() {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request body format");
    }

    /**
     * Exception handler for NullPointerExceptions due to missing fields in JSONs.
     */
    @ExceptionHandler(NullPointerException.class)
    private void missingJsonField() {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "JSON field is missing");
    }

    /**
     * Exception handler for invalid numbers.
     */
    @ExceptionHandler(NumberFormatException.class)
    private void invalidNumber() {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Cannot parse the number provided");
    }

}
