package nl.tudelft.sem.hour.management.validation;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import java.util.Set;
import lombok.Getter;
import nl.tudelft.sem.hour.management.config.GatewayConfig;
import nl.tudelft.sem.jwt.JwtUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

public class AsyncRoleValidator extends AsyncBaseValidator {
    /// Role enumeration used to check permissions (purpose: (de)serialization)
    public enum Roles {
        STUDENT,
        LECTURER,
        TA,
        ADMIN
    }

    private final transient JwtUtils jwtUtils;

    @Getter
    private final transient Set<Roles> authorizedRoles;

    /**
     * Constructs a new AsyncRoleValidator instance.
     *
     * @param gatewayConfig   The GatewayConfig.
     * @param jwtUtils        A JWTUtils library instance to help with the validation.
     */
    public AsyncRoleValidator(GatewayConfig gatewayConfig, JwtUtils jwtUtils) {
        super(gatewayConfig);
        this.jwtUtils = jwtUtils;

        this.authorizedRoles = Set.of(Roles.ADMIN);

    }

    /**
     * Constructs a new AsyncRoleValidator instance.
     *
     * @param gatewayConfig   The GatewayConfig.
     * @param jwtUtils        A JWTUtils library instance to help with the validation.
     * @param authorizedRoles List of roles that are authorized to access the endpoint.
     */
    public AsyncRoleValidator(GatewayConfig gatewayConfig,
                              JwtUtils jwtUtils, Set<Roles> authorizedRoles) {
        super(gatewayConfig);
        this.jwtUtils = jwtUtils;

        this.authorizedRoles = authorizedRoles;

    }

    @Override
    public Mono<Boolean> validate(HttpHeaders headers, String body) {
        // Because each chain start with Auth, it is fine to directly extract the JWT token
        Jws<Claims> claims = jwtUtils.validateAndParseClaims(
                jwtUtils.resolveToken(headers.getFirst("Authorization")));

        if (!authorizedRoles.contains(Roles.valueOf(jwtUtils.getRole(claims)))) {
            return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You do not have the permission to access this service."));
        }

        // Continue with the request
        return evaluateNext(headers, body);
    }
}
