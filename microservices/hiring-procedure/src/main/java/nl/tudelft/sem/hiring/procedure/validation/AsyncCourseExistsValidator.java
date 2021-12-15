package nl.tudelft.sem.hiring.procedure.validation;

import com.google.gson.JsonParser;
import java.time.ZonedDateTime;
import nl.tudelft.sem.hiring.procedure.utils.GatewayConfig;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

/**
 * Validator class for checking with the Courses microservice if a given course exists.
 * Used for methods that don't take the start time of the course in consideration.
 */
public class AsyncCourseExistsValidator extends AsyncBaseValidator {
    // Gateway configuration
    private final transient GatewayConfig gatewayConfig;

    // Course ID
    private final transient long courseId;

    // Web client used to perform requests
    private final transient WebClient webClient;

    /**
     * Constructor of the course exists validator class.
     *
     * @param gatewayConfig is the gateway configuration.
     * @param courseId      is the course ID.
     */
    public AsyncCourseExistsValidator(GatewayConfig gatewayConfig, long courseId) {
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
                .pathSegment("api", "courses", "get", "course", String.valueOf(courseId))
                .toUriString())
            .exchange()
            .flatMap(clientResponse -> {
                if (clientResponse.statusCode().isError()) {
                    return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Course not found"));
                }
                // Continue with the request
                return evaluateNext(headers, body);
            });
    }
}
