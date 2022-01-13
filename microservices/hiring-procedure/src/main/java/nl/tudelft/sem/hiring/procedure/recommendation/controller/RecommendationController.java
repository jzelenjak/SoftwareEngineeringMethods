package nl.tudelft.sem.hiring.procedure.recommendation.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import nl.tudelft.sem.hiring.procedure.recommendation.dto.RecommendationRequest;
import nl.tudelft.sem.hiring.procedure.recommendation.entities.Recommendation;
import nl.tudelft.sem.hiring.procedure.recommendation.factory.StrategyFactory;
import nl.tudelft.sem.hiring.procedure.recommendation.strategies.RecommendationStrategy;
import nl.tudelft.sem.hiring.procedure.recommendation.strategies.StrategyType;
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

    private final transient SubmissionRepository repo;

    /**
     * Instantiates a new RecommendationController object.
     *
     * @param repo          the application repository
     * @param config        the gateway configuration
     * @param jwtUtils      JWT related utilities
     */
    public RecommendationController(SubmissionRepository repo,
                                    GatewayConfig config, JwtUtils jwtUtils) {
        this.repo = repo;
        this.gatewayConfig = config;
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
     * @param req   the object with the parameters for recommendation
     * @param jwt   the jwt token of the caller
     * @return the list of recommendations for candidate TAs based on the specified metric
     *         (wrapped in the mono). The size of the list is at most `amount`.
     */
    private Mono<List<Recommendation>> recommend(RecommendationRequest req, String jwt) {
        RecommendationStrategy strategy = StrategyFactory
            .create(repo, gatewayConfig, jwt, req.getStrategy());
        return strategy.recommend(req.getCourseId(), req.getAmount(), req.getMinValue());
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
            ObjectMapper mapper = new ObjectMapper();
            RecommendationRequest req = mapper.readValue(body, RecommendationRequest.class);
            return recommend(req, authorization);
        } catch (JsonProcessingException e) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid request body format"));
        } catch (Exception e) {
            // Will be NullPointerException. Since, PMD complains about catching NPE,
            //  a general exception put here
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Missing fields in the body"));
        }
    }
}
