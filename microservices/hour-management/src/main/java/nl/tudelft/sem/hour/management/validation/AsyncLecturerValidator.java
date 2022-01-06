package nl.tudelft.sem.hour.management.validation;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import nl.tudelft.sem.hour.management.config.GatewayConfig;
import nl.tudelft.sem.jwt.JwtUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

public class AsyncLecturerValidator extends AsyncBaseValidator {

    // WebClient used to communicate with the hiring microservice
    private final transient WebClient webClient;

    // JWT utility library for parsing the authorization header
    private final transient JwtUtils jwtUtils;

    // ID of the course that the user is supposed to teach
    private final transient long courseId;

    /**
     * Constructs a new AsyncHiringValidator.
     *
     * @param gatewayConfig The gateway configuration.
     * @param jwtUtils      The JWT utility library.
     * @param courseId      The ID of the course.
     */
    public AsyncLecturerValidator(GatewayConfig gatewayConfig, JwtUtils jwtUtils,
                                  Long courseId) {
        super(gatewayConfig);
        this.jwtUtils = jwtUtils;
        this.courseId = courseId;
        this.webClient = WebClient.create();
    }

    @Override
    public Mono<Boolean> validate(HttpHeaders headers, String body) {
        // Parse the authorization token to retrieve the role and user ID
        String token = jwtUtils.resolveToken(headers.getFirst(HttpHeaders.AUTHORIZATION));
        Jws<Claims> claims = jwtUtils.validateAndParseClaims(token);

        // Admins do not have to be lecturers, therefore they should be able to bypass this check
        if (jwtUtils.getRole(claims).equals(AsyncRoleValidator.Roles.ADMIN.name())) {
            return evaluateNext(headers, body);
        }

        // Retrieve the ID of the user from the token
        Long userId = jwtUtils.getUserId(claims);

        // Initiate the request and forward the response to the next validator (if any)
        return webClient.get()
                .uri(UriComponentsBuilder.newInstance()
                        .scheme("http")
                        .host(getGatewayConfig().getHost())
                        .port(getGatewayConfig().getPort())
                        .pathSegment("api", "courses", "get", "teaches",
                                String.valueOf(userId), String.valueOf(courseId))
                        .toUriString())
                .header(HttpHeaders.AUTHORIZATION, headers.getFirst(HttpHeaders.AUTHORIZATION))
                .exchange()
                .flatMap(response -> {
                    if (response.statusCode().isError()) {
                        return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Lecturer not associated to course"));
                    }

                    // Continue with the request
                    return evaluateNext(headers, body);
                });
    }
}
