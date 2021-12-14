package nl.tudelft.sem.hiring.procedure.validation;

import com.google.gson.JsonParser;
import nl.tudelft.sem.hiring.procedure.utils.GatewayConfig;
import nl.tudelft.sem.jwt.JwtUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

public class AsyncPassingGradeValidator extends AsyncBaseValidator {
    // Minimum passing grade constant
    public static final float MIN_PASSING_GRADE = 5.75f;

    // JWT utility class for extracting info from the authorization token
    private final transient JwtUtils jwtUtils;

    // Gateway configuration
    private final transient GatewayConfig gatewayConfig;

    // Course ID
    private final transient long courseId;

    // Web client used to perform requests
    private final transient WebClient webClient;

    /**
     * Constructor of the passing grade validator class.
     *
     * @param jwtUtils      JWT utility class.
     * @param gatewayConfig is the gateway configuration.
     * @param courseId      is the id of the course.
     */
    public AsyncPassingGradeValidator(JwtUtils jwtUtils, GatewayConfig gatewayConfig,
                                      long courseId) {
        this.jwtUtils = jwtUtils;
        this.gatewayConfig = gatewayConfig;
        this.courseId = courseId;
        this.webClient = WebClient.create();
    }

    @Override
    public Mono<Boolean> validate(HttpHeaders headers, String body) {
        // Extract the user id from the authorization header
        String token = headers.getFirst(HttpHeaders.AUTHORIZATION);
        String resolvedToken = jwtUtils.resolveToken(token);
        var claims = jwtUtils.validateAndParseClaims(resolvedToken);
        long userId = jwtUtils.getUserId(claims);

        // Perform the request, and check if the user has passed the course
        return webClient.get()
                .uri(UriComponentsBuilder.newInstance()
                        .scheme("http")
                        .host(gatewayConfig.getHost())
                        .port(gatewayConfig.getPort())
                        .pathSegment("api", "courses", "grade")
                        .queryParam("courseId", courseId)
                        .queryParam("userId", userId)
                        .toUriString())
                .exchange()
                .flatMap(clientResponse -> {
                    if (clientResponse.statusCode().isError()) {
                        return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Course/grade not found"));
                    }

                    return clientResponse.bodyToMono(String.class).flatMap(responseBody -> {
                        var response = JsonParser.parseString(responseBody).getAsJsonObject();
                        var grade = response.get("grade").getAsFloat();

                        // Check if the user passed the course
                        if (grade < MIN_PASSING_GRADE) {
                            return Mono.error(
                                    new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED,
                                            "You cannot apply to be a TA for this course since "
                                                    + "you did not pass it yet"));
                        }

                        // Continue with the request
                        return evaluateNext(headers, body);
                    });
                });
    }
}
