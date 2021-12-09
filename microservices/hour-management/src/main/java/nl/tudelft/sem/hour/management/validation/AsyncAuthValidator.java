package nl.tudelft.sem.hour.management.validation;

import nl.tudelft.sem.hour.management.config.GatewayConfig;
import nl.tudelft.sem.jwt.JwtUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

public class AsyncAuthValidator extends AsyncBaseValidator {

    // JWT utility token
    private final transient JwtUtils jwtUtils;

    /**
     * Constructor.
     *
     * @param gatewayConfig The GatewayConfig.
     * @param jwtUtils      A JWTUtils library instance to help with the validation.
     */
    public AsyncAuthValidator(GatewayConfig gatewayConfig, JwtUtils jwtUtils) {
        super(gatewayConfig);
        this.jwtUtils = jwtUtils;
    }

    @Override
    public Mono<Boolean> validate(HttpHeaders headers, String body) {
        String authorization = headers.getFirst("Authorization");
        if (authorization == null) {
            return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "User unauthorized, please login before proceeding."));
        }

        // check whether the authorization token is valid
        String token = jwtUtils.resolveToken(authorization);

        if (token == null) {
            return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Invalid JWT token, please try again."));
        }

        // Check validity of JWT token, in case of failure, return error
        if (jwtUtils.validateAndParseClaims(token) == null) {
            return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Invalid authorization token. Try to login before proceeding."));
        }

        // Continue with the request
        return evaluateNext(headers, body);
    }
}
