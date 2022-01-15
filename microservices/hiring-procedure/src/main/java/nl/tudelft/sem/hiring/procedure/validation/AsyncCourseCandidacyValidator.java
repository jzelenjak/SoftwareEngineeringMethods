package nl.tudelft.sem.hiring.procedure.validation;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import nl.tudelft.sem.hiring.procedure.entities.Submission;
import nl.tudelft.sem.hiring.procedure.services.SubmissionService;
import nl.tudelft.sem.hiring.procedure.utils.GatewayConfig;
import nl.tudelft.sem.jwt.JwtUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

public class AsyncCourseCandidacyValidator extends AsyncBaseValidator {

    private static final Long MAX_SUBMISSIONS_PER_QUARTER = 3L;

    private final transient JwtUtils jwtUtils;

    private final transient SubmissionService submissionService;

    private final transient GatewayConfig gatewayConfig;

    private final transient long courseId;

    private final transient WebClient webClient;

    /**
     * Constructor of the AsyncCourseCandidacyValidator class.
     *
     * @param jwtUtils          JWT utility library.
     * @param submissionService The application service.
     * @param gatewayConfig     is the gateway configuration.
     * @param courseId          is the course ID.
     */
    public AsyncCourseCandidacyValidator(JwtUtils jwtUtils, SubmissionService submissionService,
                                         GatewayConfig gatewayConfig, long courseId) {
        this.jwtUtils = jwtUtils;
        this.submissionService = submissionService;
        this.gatewayConfig = gatewayConfig;
        this.courseId = courseId;
        this.webClient = WebClient.create();
    }

    @Override
    public Mono<Boolean> validate(HttpHeaders headers, String body) {
        // Create the body for the request
        Long userId = getUserIdFromAuthorizationHeader(headers.getFirst(HttpHeaders.AUTHORIZATION));
        JsonObject requestBody = unreviewedSubmissionCoursesToJson(userId, courseId);

        // Perform the request to the courses microservice
        return webClient.post()
                .uri(UriComponentsBuilder.newInstance()
                        .scheme("http")
                        .host(gatewayConfig.getHost())
                        .port(gatewayConfig.getPort())
                        .pathSegment("api", "courses", "get-multiple")
                        .toUriString())
                .header(HttpHeaders.AUTHORIZATION, headers.getFirst(HttpHeaders.AUTHORIZATION))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(requestBody.toString()), String.class)
                .exchange()
                .flatMap(clientResponse -> {
                    if (clientResponse.statusCode().isError()) {
                        return Mono.error(new ResponseStatusException(clientResponse.statusCode(),
                                "Error while getting information for multiple courses"));
                    }

                    return clientResponse.bodyToMono(String.class).flatMap(responseBody -> {
                        // Extract the maximum submissions per quarter
                        Optional<Long> maximumOccurrence = extractMaximumSubmissionsPerQuarter(
                                JsonParser.parseString(responseBody).getAsJsonObject()
                        );

                        // If there is no maximum start occurrence, the request is valid. The
                        // same applies if there are less than, or equal to, 3 courses with the
                        // same start date.
                        if (maximumOccurrence.isPresent()
                                && maximumOccurrence.get() > MAX_SUBMISSIONS_PER_QUARTER) {
                            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                    "A student cannot candidate themselves for more than 3 "
                                            + "courses per quarter"));
                        }

                        return evaluateNext(headers, body);
                    });
                });
    }

    /**
     * Attempts to extract the maximum amount of submissions per quarter from the response of
     * the course microservice.
     *
     * <p>The start date, truncated to days, is used as a means to group the courses by quarter.
     *
     * @param response is the response from the course microservice.
     * @return the maximum amount of submissions per quarter, if exists.
     */
    private Optional<Long> extractMaximumSubmissionsPerQuarter(JsonObject response) {
        return response.entrySet()
                .stream()
                .map(entry -> entry.getValue().getAsJsonObject().get("startDate"))
                .map(date -> ZonedDateTime.parse(date.getAsString())
                        .truncatedTo(ChronoUnit.DAYS))
                .collect(Collectors.groupingBy(Function.identity(),
                        Collectors.counting()))
                .values()
                .stream()
                .max(Comparator.comparingLong(Long::longValue));
    }

    /**
     * Retrieves the unreviewed submissions for the given user. Since a lecturer will only review
     * submissions after the submission deadline has passed, we can use this 'metric' to determine
     * whether the student has too many open submissions.
     *
     * <p>The returned JSON request body contains a list of course IDs that can be used to fetch
     * information about those courses from the course microservice.
     *
     * @param userId   is the ID of the user whose unreviewed submissions are to be retrieved.
     * @param courseId is the ID of the course that the user wants to enroll for.
     * @return a JSON request body that can be used to retrieve course info for the courses
     *         associated to the unreviewed submissions of the user.
     */
    private JsonObject unreviewedSubmissionCoursesToJson(Long userId, Long courseId) {
        // Retrieve the course IDs from all unreviewed submissions.
        // We use these to fetch information from the course microservice.
        List<Long> courseIds = submissionService.getUnreviewedSubmissionsForUser(userId).stream()
                .map(Submission::getCourseId)
                .collect(Collectors.toList());

        // We want to know if our new submission is the 'drop that causes the bucket to overflow'.
        // Hence, we need to add the course ID for the new submission as well.
        courseIds.add(courseId);

        // Compose the JSON request body and return it
        JsonObject json = new JsonObject();
        json.add("courseIds", new Gson().toJsonTree(courseIds));
        return json;
    }

    /**
     * Retrieves the user ID from the authorization token with help of the JWT utility library.
     *
     * @param authorizationHeader is the authorization header of the request.
     * @return the user ID of the user that is associated with the authorization token.
     */
    private Long getUserIdFromAuthorizationHeader(String authorizationHeader) {
        String resolveToken = jwtUtils.resolveToken(authorizationHeader);
        return jwtUtils.getUserId(jwtUtils.validateAndParseClaims(resolveToken));
    }

}
