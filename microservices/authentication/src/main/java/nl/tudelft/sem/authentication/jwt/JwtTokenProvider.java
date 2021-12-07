package nl.tudelft.sem.authentication.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import java.security.Key;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import nl.tudelft.sem.authentication.security.UserRole;
import nl.tudelft.sem.authentication.service.AuthService;
import nl.tudelft.sem.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * A class that provides utilities related to JWT token.
 */
@Component
public class JwtTokenProvider {

    private final transient Key hmacKey;

    @Value("${jwtTokenValidityInMinutes:10}")
    private transient long validityInMinutes;

    private final transient AuthService authService;

    private final transient JwtUtils jwtUtils;

    /**
     * Instantiates JwtTokenProvider object.
     *
     * @param hmacKey   The secret key used to sign the token
     */
    public JwtTokenProvider(AuthService authService, @Qualifier("secretKey") Key hmacKey, JwtUtils jwtUtils) {
        this.authService = authService;
        this.hmacKey = hmacKey;
        this.jwtUtils = jwtUtils;
    }

    /**
     * Creates a new JWT.
     *
     * @param userId   the userId of the user
     * @param role     the role of the user (STUDENT, LECTURER, TA, ADMIN)
     * @param date     the date of issue of the token
     * @return signed JWT
     */
    public String createToken(long userId, UserRole role, Date date) {
        Claims claims = Jwts.claims().setSubject(String.valueOf(userId));
        claims.put("role", role);
        Date validity = new Date(date.getTime() + validityInMinutes * 60000);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(date)
                .setExpiration(validity)
                .signWith(this.hmacKey)
                .compact();
    }

    /**
     * Gets a JWT from a request's Authorization header.
     *
     * @param req the request.
     * @return The JWT in the request, null if no JWT was found or there is no 'Bearer ' prefix.
     */
    public String resolveToken(HttpServletRequest req) {
        String bearerToken = req.getHeader(HttpHeaders.AUTHORIZATION);
        return jwtUtils.resolveToken(bearerToken);
    }

    /**
     * Validates a JWT.
     *
     * @param token JWT to validate
     * @return true if the token is valid (not expired and not corrupted)
     *         false otherwise
     */
    public boolean validateToken(String token) {
        return this.validateAndParseToken(token) != null;
    }

    /**
     * Validates a JWT. Returns the claims
     *
     * @param token JWT to validate
     * @return true if the token is valid (not expired and not corrupted)
     *         false otherwise
     */
    public Jws<Claims> validateAndParseToken(String token) {
        return jwtUtils.validateAndParseClaims(token);
    }

    /**
     * Gets the username from the user with a given JWT token.
     *
     * @param token the JWT to get the username from.
     * @return the username from the JWT.
     */
    public String getSubject(String token) {
        Jws<Claims> claimsJws = Jwts.parserBuilder()
                .setSigningKey(this.hmacKey)
                .build()
                .parseClaimsJws(token);
        return this.getSubject(claimsJws);
    }

    /**
     * Gets the username from the user with a given JWT token.
     *
     * @param claimsJws          .
     * @return the username from the JWT.
     */
    public String getSubject(Jws<Claims> claimsJws) {
        return claimsJws
                .getBody()
                .getSubject();
    }

    /**
     * Gets the role from the user with the given JWT token.
     * Assumes that the provided token is valid (check this with 'validate' method).
     *
     * @param token the JWT token (assumed to be valid)
     * @return the role from JWT (STUDENT, LECTURER, TA, ADMIN)
     */
    public String getRole(String token) {
        Jws<Claims> claimsJws = Jwts
                .parserBuilder()
                .setSigningKey(this.hmacKey)
                .build()
                .parseClaimsJws(token);
        return getRole(claimsJws);
    }

    /**
     * Gets the role from the user with the given JWT token.
     * Assumes that the provided token is valid (check this with 'validate' method).
     *
     * @param claimsJws  dummy
     * @return the role from JWT (STUDENT, LECTURER, TA, ADMIN)
     */
    public String getRole(Jws<Claims> claimsJws) {
        return claimsJws
                .getBody().get("role").toString();
    }

    /**
     * Gets the authentication of a JWT.
     *
     * @param token the JWT to get the authentication from.
     * @return the authentication in the JWT.
     */
    public Authentication getAuthentication(String token) {
        Jws<Claims> claims = this.validateAndParseToken(token);
        if (claims == null) {
            return null;
        }
        return this.getAuthentication(claims);
    }

    public String getUsername(String token) {
        Jws<Claims> claims = this.validateAndParseToken(token);
        if (claims == null) {
            return null;
        }
        long userId = Long.parseLong(this.getSubject(claims));

        return this.authService.loadUserByUserId(userId).getUsername();
    }

    public Authentication getAuthentication(Jws<Claims> claimsJws) {
        long userId = Long.parseLong(this.getSubject(claimsJws));

        UserDetails userDetails = this.authService.loadUserByUserId(userId);

        return new UsernamePasswordAuthenticationToken(userDetails,"", userDetails.getAuthorities());
    }
}

