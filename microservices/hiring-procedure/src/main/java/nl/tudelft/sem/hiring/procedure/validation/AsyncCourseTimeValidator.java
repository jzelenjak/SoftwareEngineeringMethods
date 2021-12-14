package nl.tudelft.sem.hiring.procedure.validation;

import com.google.gson.JsonParser;
import java.time.Period;
import java.time.ZonedDateTime;
import nl.tudelft.sem.hiring.procedure.utils.GatewayConfig;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

public class AsyncCourseTimeValidator extends AsyncBaseValidator {
    /// Duration used to validate course correctness (3 weeks in advance)
    public static final Period VALID_DURATION = Period.ofWeeks(3);

    // Gateway configuration
    private final transient GatewayConfig gatewayConfig;

    // Course ID
    private final transient long courseId;

    // Web client used to perform requests
    private final transient WebClient webClient;

    /**
     * Constructor of the course time validator class.
     *
     * @param gatewayConfig is the gateway configuration.
     * @param courseId      is the course ID.
     */
    public AsyncCourseTimeValidator(GatewayConfig gatewayConfig, long courseId) {
        this.gatewayConfig = gatewayConfig;
        this.courseId = courseId;
        this.webClient = WebClient.create();
    }

    @Override
    public Mono<Boolean> validate(HttpHeaders headers, String body) {
        return webClient.get()
                .uri(UriComponentsBuilder.newInstance()
                        .scheme("http")
                        .host(gatewayConfig.getHost())
                        .port(gatewayConfig.getPort())
                        .pathSegment("api", "courses", "get-start-date")
                        .queryParam("courseId", courseId)
                        .toUriString())
                .exchange()
                .flatMap(clientResponse -> {
                    if (clientResponse.statusCode().isError()) {
                        return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Course not found"));
                    }

                    return clientResponse.bodyToMono(String.class).flatMap(responseBody -> {
                        var response = JsonParser.parseString(responseBody).getAsJsonObject();
                        var start = ZonedDateTime.parse(response.get("startTime").getAsString());

                        // Check if the course registration period has not ended yet
                        if (ZonedDateTime.now().plus(VALID_DURATION).isAfter(start)) {
                            return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN,
                                    "Course registration/withdrawal period has ended"));
                        }

                        // Continue with the request
                        return evaluateNext(headers, body);
                    });
                });
    }

}
