package nl.tudelft.sem.hour.management.validation;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

public class AsyncHiringValidator extends AsyncBaseValidator {

    // WebClient used to communicate with the hiring microservice
    private final transient WebClient webClient;

    // JWT utility library for parsing the authorization header
    private final transient JwtUtils jwtUtils;

    /**
     * Constructs a new AsyncHiringValidator.
     *
     * @param gatewayConfig The gateway configuration.
     * @param jwtUtils      The JWT utility library.
     */
    public AsyncHiringValidator(GatewayConfig gatewayConfig, JwtUtils jwtUtils) {
        super(gatewayConfig);
        this.jwtUtils = jwtUtils;
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

        // Verify body of recorded request
        JsonObject parsed = JsonParser.parseString(body).getAsJsonObject();

        // Initiate the request and forward the response to the next validator (if any)
        return webClient.get()
                .uri(UriComponentsBuilder.newInstance()
                        .scheme("http")
                        .host(getGatewayConfig().getHost())
                        .port(getGatewayConfig().getPort())
                        .pathSegment("api", "hiring-service", "get-contract")
                        .queryParam("courseID", parsed.get("courseId"))
                        .toUriString())
                .header(HttpHeaders.AUTHORIZATION, headers.getFirst(HttpHeaders.AUTHORIZATION))
                .exchange()
                .flatMap(response -> {
                    if (response.statusCode().isError()) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "Cannot find active contract"));
                    }

                    return response.bodyToMono(String.class).flatMap(json -> {
                        JsonObject contract = JsonParser.parseString(json).getAsJsonObject();

                        if (parsed.get("declaredHours").getAsDouble() <= 0) {
                            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                    "Declared hours cannot be negative or 0."));
                        }

                        if (parsed.get("declaredHours").getAsDouble()
                                > contract.get("maxHours").getAsDouble()) {
                            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                    "Declared hours cannot exceed the maximum hours "
                                            + "denoted in the contract."));
                        }
                        return evaluateNext(headers, body);
                    });
                });
    }
}
