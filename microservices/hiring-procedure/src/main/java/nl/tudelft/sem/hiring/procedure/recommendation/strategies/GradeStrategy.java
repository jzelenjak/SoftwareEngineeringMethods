package nl.tudelft.sem.hiring.procedure.recommendation.strategies;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import nl.tudelft.sem.hiring.procedure.recommendation.entities.Recommendation;
import nl.tudelft.sem.hiring.procedure.repositories.SubmissionRepository;
import nl.tudelft.sem.hiring.procedure.utils.GatewayConfig;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

/**
 * The class that extends BaseStrategy class and implements RecommendationStrategy interface by
 *  recommending candidate TAs based of the max grade they have received for the given course.
 */
public class GradeStrategy extends RecommendationStrategyBase {

    /**
     * Instantiates a new GradeStrategy object.
     *
     * @param repo          the TA application repository.
     * @param gatewayConfig the gateway configuration.
     * @param authorization the authorization token of the caller.
     */
    public GradeStrategy(SubmissionRepository repo, GatewayConfig gatewayConfig,
                         String authorization) {
        super(repo, gatewayConfig, authorization);
    }

    /**
     * Recommends at most the specified number of candidate TAs who have applied
     * for a specified course.
     * It uses the strategy of the highest grade for the given course.
     *
     * @param courseId the id of the course
     * @param amount   the maximum number of recommendations to return
     * @param minGrade the minimum grade (used for filtering)
     * @return the list of recommendations for candidate TAs based on the highest grade
     *         for the given course (wrapped in the mono).
     *         The size of the list is at most `amount`.
     */
    @Override
    public Mono<List<Recommendation>> recommend(long courseId, int amount, double minGrade) {
        List<Long> applicants = repo.findAllApplicantsByCourseId(courseId);

        if (applicants.isEmpty()) {
            return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Could not find any applicants"));
        }

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode()
                .put("courseId", courseId)
                .put("amount", amount)
                .put("minGrade", minGrade);
        node.set("userIds", mapper.valueToTree(applicants));

        String uri = buildUri(gatewayConfig.getHost(), gatewayConfig.getPort(),
            "api", "courses", "statistics", "user-grade");
        return this.post(uri, node.toString(), authorization)
                .flatMap(response -> processMono(response, this::processMonoBody));
    }

    /**
     * A helper method to process the received mono response body.
     *
     * @param body the body from the received mono response
     * @return the list of recommendations for candidate TAs based on the highest
     *         grade for the given course (wrapped in the mono).
     */
    private Mono<List<Recommendation>> processMonoBody(String body) {
        try {
            return recommendationsToMono(convertJsonToRecommendationList(body));
        } catch (JsonProcessingException e) {
            return Mono
                    .error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                            "An error has occurred. Please try again later!"));
        }
    }
}
