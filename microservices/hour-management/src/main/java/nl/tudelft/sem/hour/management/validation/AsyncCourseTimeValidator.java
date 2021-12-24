package nl.tudelft.sem.hour.management.validation;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.time.Period;
import java.time.ZonedDateTime;
import nl.tudelft.sem.hour.management.config.GatewayConfig;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

public class AsyncCourseTimeValidator extends AsyncBaseValidator {
    // Valid period for performing actions on the course
    private static final Period VALID_DURATION = Period.ofMonths(3);

    // WebClient used to communicate with the hiring microservice
    private final transient WebClient webClient;

    /**
     * Constructs a new AsyncHiringValidator.
     *
     * @param gatewayConfig The gateway configuration.
     */
    public AsyncCourseTimeValidator(GatewayConfig gatewayConfig) {
        super(gatewayConfig);
        this.webClient = WebClient.create();
    }

    @Override
    public Mono<Boolean> validate(HttpHeaders headers, String body) {
        // Verify body of recorded request
        JsonObject parsed = JsonParser.parseString(body).getAsJsonObject();

        // Initiate the request and forward the response to the next validator (if any)
        return webClient.get()
                .uri(UriComponentsBuilder.newInstance()
                        .scheme("http")
                        .host(getGatewayConfig().getHost())
                        .port(getGatewayConfig().getPort())
                        .pathSegment("api", "courses", "get", parsed.get("courseId").getAsString())
                        .toUriString())
                .exchange()
                .flatMap(response -> {
                    if (response.statusCode().isError()) {
                        return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Course not found"));
                    }

                    return response.bodyToMono(String.class).flatMap(responseBody -> {
                        JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
                        ZonedDateTime start = ZonedDateTime
                                .parse(json.get("startDate").getAsString());
                        ZonedDateTime end = ZonedDateTime.parse(json.get("endDate").getAsString());

                        if (ZonedDateTime.now().isBefore(start)) {
                            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                    "Course hasn't started yet"));
                        }

                        // Check if the course + grace period has ended
                        if (ZonedDateTime.now().minus(VALID_DURATION).isAfter(end)) {
                            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                    "Course does no longer accept declarations"));
                        }

                        // Continue with the request
                        return evaluateNext(headers, body);
                    });
                });
    }
}
