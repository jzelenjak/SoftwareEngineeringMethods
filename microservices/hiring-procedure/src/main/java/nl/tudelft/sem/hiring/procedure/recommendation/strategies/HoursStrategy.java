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
 * The class that extends BaseStrategy class and implements RecommendationStrategy interface
 *  by recommending candidate TAs based of the total number of hours they have been working
 *  as a TA for a given course.
 */
public class HoursStrategy extends RecommendationStrategyBase {

    /**
     * Instantiates a new HoursStrategy object.
     *
     * @param repo          the TA application repository.
     * @param gatewayConfig the gateway configuration.
     * @param authorization the authorization token of the caller.
     */
    public HoursStrategy(SubmissionRepository repo, GatewayConfig gatewayConfig,
                         String authorization) {
        super(repo, gatewayConfig, authorization);
    }

    /**
     * Recommends at most the specified number of candidate TAs who have worked
     * the most for a specified course.
     * It uses the strategy of the most hours worked as a TA for a given course.
     *
     * @param courseId the id of the course
     * @param amount   the maximum number of recommendations to return
     * @param minHours the minimum number of worked hours (used for filtering)
     * @return the list of recommendations for candidate TAs based on the number of
     *         hours worked as a TA for a given course (wrapped in the mono).
     *         The size of the list is at most `amount`.
     */
    @Override
    public Mono<List<Recommendation>> recommend(long courseId, int amount, double minHours) {
        List<Long> applicants = repo.findAllApplicantsByCourseId(courseId);

        if (applicants.isEmpty()) {
            return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Could not find any applicants"));
        }

        String uri = buildUriWithCourseId(gatewayConfig.getHost(), gatewayConfig.getPort(),
            courseId, "api", "courses", "get-all-editions");
        return this.get(uri, authorization)
                .flatMap(response -> processMono(response, body ->
                                processMonoBodyFromCourses(body, amount, minHours, applicants)));
    }

    /**
     * A helper method to process the mono body received from Courses microservice,
     * send a request to the Hour Management microservice and process the response from it.
     *
     * @param body     the mono body received from Courses microservices
     * @param amount   the maximum number of recommendations to return
     * @param minHours the minimum number of worked hours (used for filtering)
     * @param userIds  the list of applicants' user IDs
     * @return the list of recommendations for candidate TAs based on the number of
     *         hours worked as a TA for a given course (wrapped in the mono).
     *         The size of the list is at most `amount`.
     */
    private Mono<List<Recommendation>> processMonoBodyFromCourses(String body,
                                                                  int amount, double minHours,
                                                                  List<Long> userIds) {
        try {
            List<Long> courseIds = convertJsonToLongList(body, "courseIds");

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode objectNode = mapper.createObjectNode();
            objectNode.put("minHours", minHours);
            objectNode.put("amount", amount);
            objectNode.set("userIds", mapper.valueToTree(userIds));
            objectNode.set("courseIds", mapper.valueToTree(courseIds));

            String uri = buildUri(gatewayConfig.getHost(), gatewayConfig.getPort(),
                "api", "hour-management", "statistics", "total-user-hours");
            return this.post(uri, objectNode.toString(), authorization)
                    .flatMap(response ->
                            processMono(response, this::processMonoBodyFromHours));
        } catch (Exception e) {
            // related to JSON processing (e.g. bad format, missing/incorrect fields)
            return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                            "An error has occurred. Please try again later!"));
        }
    }

    /**
     * A helper method to process the mono body received from Hours Microservice.
     *
     * @param body the mono body received from Hours Microservice
     * @return the list of recommendations for candidate TAs based on the number of
     *         hours worked as a TA for a given course (wrapped in the mono).
     *         The size of the list is at most `amount`.
     */
    private Mono<List<Recommendation>> processMonoBodyFromHours(String body) {
        try {
            return recommendationsToMono(convertJsonToRecommendationList(body));
        } catch (JsonProcessingException e) {
            return Mono
                    .error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                            "An error has occurred. Please try again later!"));
        }
    }
}
