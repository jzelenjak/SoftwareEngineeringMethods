package nl.tudelft.sem.hiring.procedure.validation;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import nl.tudelft.sem.hiring.procedure.entities.Application;
import nl.tudelft.sem.hiring.procedure.services.ApplicationService;
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

    private final transient JwtUtils jwtUtils;

    private final transient ApplicationService applicationService;

    private final transient GatewayConfig gatewayConfig;

    private final transient long courseId;

    private final transient WebClient webClient;

    /**
     * Constructor of the AsyncCourseCandidacyValidator class.
     *
     * @param jwtUtils           JWT utility library.
     * @param applicationService The application service.
     * @param gatewayConfig      is the gateway configuration.
     * @param courseId           is the course ID.
     */
    public AsyncCourseCandidacyValidator(JwtUtils jwtUtils, ApplicationService applicationService,
                                         GatewayConfig gatewayConfig, long courseId) {
        this.jwtUtils = jwtUtils;
        this.applicationService = applicationService;
        this.gatewayConfig = gatewayConfig;
        this.courseId = courseId;
        this.webClient = WebClient.create();
    }

    @Override
    public Mono<Boolean> validate(HttpHeaders headers, String body) {
        // Retrieve the user ID from the authorization header
        String token = jwtUtils.resolveToken(headers.getFirst(HttpHeaders.AUTHORIZATION));
        Jws<Claims> claims = jwtUtils.validateAndParseClaims(token);
        long userId = jwtUtils.getUserId(claims);

        // Compose the body for the request
        JsonObject requestBody = new JsonObject();
        List<Long> courseIds = applicationService.getUnreviewedApplicationsForUser(userId)
                .stream()
                .map(Application::getCourseId)
                .collect(Collectors.toList());

        // Add the course ID since we have to use it for validation
        courseIds.add(courseId);
        requestBody.add("courseIds", new Gson().toJsonTree(courseIds));

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
                        // Parse the response
                        var response = JsonParser.parseString(responseBody).getAsJsonObject();

                        // Check if we have received a valid response (info for all courses)
                        if (response.size() != courseIds.size()) {
                            return Mono.error(new ResponseStatusException(
                                    HttpStatus.INTERNAL_SERVER_ERROR,
                                    "Validator unable to retrieve course information for all "
                                            + "requested courses"));
                        }

                        // Group all courses by their start date, if there are 4 courses with the
                        // same start date, the request is invalid
                        Optional<Long> maximumOccurrence = response
                                .entrySet()
                                .stream()
                                .map(entry -> entry.getValue().getAsJsonObject().get("startDate"))
                                .map(date -> ZonedDateTime.parse(date.getAsString())
                                        .truncatedTo(ChronoUnit.DAYS))
                                .collect(Collectors.groupingBy(Function.identity(),
                                        Collectors.counting()))
                                .values()
                                .stream()
                                .max(Comparator.comparingLong(Long::longValue));

                        // If there is no maximum start occurrence, the request is valid. The
                        // same applies if there are less than, or equal to, 3 courses with the
                        // same start date.
                        if (maximumOccurrence.isPresent() && maximumOccurrence.get() > 3L) {
                            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                    "A student cannot candidate themselves for more than 3 "
                                            + "courses per quarter"));
                        }

                        return evaluateNext(headers, body);
                    });
                });
    }
}
