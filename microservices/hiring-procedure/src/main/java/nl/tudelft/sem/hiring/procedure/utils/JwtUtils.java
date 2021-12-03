package nl.tudelft.sem.hiring.procedure.utils;

import io.jsonwebtoken.*;
import java.security.Key;
import java.util.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * A class that provides utilities related to JWT.
 */
@Component
public class JwtUtils {

    private final transient Key hmacKey;

    @Value("${jwtTokenValidityInMinutes}")
    private transient long validityInMinutes;

    /**
     * Instantiates JwtUtils object.
     *
     * @param hmacKey   The secret key used to sign the token
     */
    public JwtUtils(@Qualifier("secretKey") Key hmacKey) {
        this.hmacKey = hmacKey;
    }

    /**
     * Gets a JWT from a request's Authorization header.
     *
     * @param jwtPrefixed JWT token with prefix 'Bearer '
     * @return The JWT in the request, null if no JWT was found or there is no 'Bearer ' prefix.
     */
    public String resolveToken(String jwtPrefixed) {
        if (jwtPrefixed == null || !jwtPrefixed.startsWith("Bearer ")) {
            return null;
        }
        return jwtPrefixed.substring(7);
    }

    /**
     * Validates a JWT.
     *
     * @param token JWT to validate
     * @return True if the token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts
                .parserBuilder()
                .setSigningKey(this.hmacKey)
                .build()
                .parseClaimsJws(token);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Gets the username from a JWT.
     *
     * @param token the JWT to get the username from.
     * @return the username from the JWT.
     */
    public String getUsername(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(this.hmacKey)
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
    }

    /**
     * Gets the role from JWT token.
     * Assumes that the token is valid (check this with 'validate' method above).
     *
     * @param token the JWT token (assumed to be valid)
     * @return the role from JWT (STUDENT, LECTURER, TA, ADMIN)
     */
    public String getRole(String token) {
        return Jwts
            .parserBuilder()
            .setSigningKey(this.hmacKey)
            .build()
            .parseClaimsJws(token)
            .getBody().get("role").toString();
    }
}
