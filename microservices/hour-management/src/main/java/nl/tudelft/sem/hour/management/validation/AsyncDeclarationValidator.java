package nl.tudelft.sem.hour.management.validation;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import nl.tudelft.sem.hour.management.dto.HourDeclarationRequest;
import nl.tudelft.sem.hour.management.validation.AsyncRoleValidator.Roles;
import nl.tudelft.sem.jwt.JwtUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

public class AsyncDeclarationValidator extends AsyncBaseValidator {

    private final transient JwtUtils jwtUtils;

    private final transient HourDeclarationRequest declarationRequest;

    /**
     * Constructs a new AsyncDeclarationValidator instance.
     *
     * @param jwtUtils           The JWT utility library.
     * @param declarationRequest The declaration request.
     */
    public AsyncDeclarationValidator(JwtUtils jwtUtils, HourDeclarationRequest declarationRequest) {
        super(null);
        this.jwtUtils = jwtUtils;
        this.declarationRequest = declarationRequest;
    }

    @Override
    public Mono<Boolean> validate(HttpHeaders headers, String body) {
        // Extract claims from token
        String token = headers.getFirst(HttpHeaders.AUTHORIZATION);
        String resolveToken = jwtUtils.resolveToken(token);
        Jws<Claims> claims = jwtUtils.validateAndParseClaims(resolveToken);

        // Retrieve the role and user ID and perform validation
        if (Roles.valueOf(jwtUtils.getRole(claims)) == Roles.STUDENT
                && declarationRequest.getStudentId() != jwtUtils.getUserId(claims)) {
            return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You are not allowed to declare hours for other users."));
        }

        // Continue with the request
        return evaluateNext(headers, body);
    }

}
