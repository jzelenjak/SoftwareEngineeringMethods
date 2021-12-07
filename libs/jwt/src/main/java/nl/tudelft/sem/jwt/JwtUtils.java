package nl.tudelft.sem.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.security.Key;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;


/**
 * The JwtUtils class is a class that provides some utilities to parse,
 *   validate and get data from a JWT token.
 *
 * <p><b>Important:</b> To be able to use these utilities:</p>
 * <ul>
 *     <li>
 *         Add <i>implementation project(':libs:jwt')</i>
 *              into the build.gradle file in your microservice
 *     </li>
 *     <li>
 *         Add <i>@Import(HeartbeatConfig.class)</i> above your Main Application class.
 *         If you already have an <i>@Import(...)</i> above your Main Application class,
 *         then include it into the list of classes in <i>@Import</i> like this:
 *         <i>@Import({JwtUtils.class, HeartbeatConfig.class})</i>
 *     </li>
 * </ul>
 */
@PropertySource(value = "classpath:jwt_default.properties")
public class JwtUtils {

    private final transient Key secretKey;

    @Autowired
    public JwtUtils(@Value("${jwt.secretKeyString}") String secretKeyString) {
        this.secretKey = new SecretKeySpec(secretKeyString.getBytes(),
                SignatureAlgorithm.HS256.getJcaName());
    }

    /**
     * Gets a JWT from a request's Authorization header.
     *
     * @param bearerToken   token prefixed with 'Bearer '.
     * @return The JWT in the request, null if no JWT was found or there is no 'Bearer ' prefix.
     */
    public String resolveToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }


    /**
     * Validates a JWT, and returns the parsed claims if the token is valid.
     * NB! Assumes that the token is already without a prefix.
     *     Call resolveToken method above first.
     *
     * @param token     JWT to validate
     * @return the parsed claims from the token if it is valid (not expired and not corrupted),
     *         null otherwise
     */
    public Jws<Claims> validateAndParseClaims(String token) {
        try {
            Jws<Claims> claims =
                    Jwts
                    .parserBuilder()
                    .setSigningKey(this.secretKey)
                    .build()
                    .parseClaimsJws(token);
            Long.parseLong(claims.getBody().getSubject());
            return claims;
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * Gets the username from the parsed JWS claims.
     * NB! Assumes that the token from which the claims have been parsed is valid.
     *     Call validateAndParseClaims method above first.
     *
     * @param claims    parsed JWS claims.
     * @return the userId from the parsed JWS claims.
     */
    public long getUserId(Jws<Claims> claims) {
        return Long.parseLong(claims.getBody().getSubject());
    }

    /**
     * Gets the role of the user from the parsed JWS claims.
     * NB! Assumes that the token from which the claims have been parsed is valid.
     *     Call validateAndParseClaims method above first.
     *
     * @param claims     parsed JWS claims.
     * @return the role of the user from JWT (STUDENT, LECTURER, TA, ADMIN)
     */
    public String getRole(Jws<Claims> claims) {
        return claims
                .getBody()
                .get("role")
                .toString();
    }

}
