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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * A class that provides utilities related to JWT token. It uses the JWT library as well.
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
    public JwtTokenProvider(AuthService authService,
                            @Qualifier("secretKey") Key hmacKey, JwtUtils jwtUtils) {
        this.authService = authService;
        this.hmacKey = hmacKey;
        this.jwtUtils = jwtUtils;
    }

    /**
     * Creates a new JWT token.
     *
     * @param userId   the userId of the user
     * @param role     the role of the user (STUDENT, CANDIDATE_TA, TA, LECTURER, ADMIN)
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
     * Gets a JWT from the HTTP request's 'Authorization' header.
     *
     * @param req       the HTTP request.
     * @return The JWT in the request, null if no JWT was found or there is no 'Bearer ' prefix.
     */
    public String resolveToken(HttpServletRequest req) {
        String bearerToken = req.getHeader(HttpHeaders.AUTHORIZATION);
        return jwtUtils.resolveToken(bearerToken);
    }

    /**
     * Validates a JWT. Takes in an unprefixed token String.
     *
     * @param token     JWT to validate (unprefixed)
     * @return true if the token is valid (not expired and not corrupted)
     *         false otherwise
     */
    public boolean validateToken(String token) {
        return this.validateAndParseToken(token) != null;
    }

    /**
     * Validates a JWT and parses it. Returns the claims from the token.
     *
     * @param token     JWT to validate
     * @return true if the token is valid (not expired and not corrupted)
     *         false otherwise
     */
    public Jws<Claims> validateAndParseToken(String token) {
        return jwtUtils.validateAndParseClaims(token);
    }

    /**
     * Gets the subject (here: userId) from the user with a given JWT token.
     * Takes in an unprefixed token String.
     *
     * @param token     the JWT to get the subject from.
     * @return the subject from the JWT (NB! As a String).
     */
    public String getSubject(String token) {
        Jws<Claims> claimsJws = this.validateAndParseToken(token);
        return this.getSubject(claimsJws);
    }

    /**
     * Gets the subject (here: userId) from the user with a given JWT token.
     * Takes in the claims from a valid parsed token (call validate first).
     *
     * @param claimsJws     the claims from a parsed token   .
     * @return the subject from the JWT (NB! As a String).
     */
    public String getSubject(Jws<Claims> claimsJws) {
        return String.valueOf(jwtUtils.getUserId(claimsJws));
    }

    /**
     * Gets the role of the user from a given JWT token. Takes in the token as a String.
     * Assumes that the provided token is valid (check this with 'validate' method).
     *
     * @param token the JWT token string (assumed to be valid)
     * @return the role from JWT (STUDENT, CANDIDATE_TA, TA, LECTURER, ADMIN)
     */
    public String getRole(String token) {
        Jws<Claims> claimsJws = this.validateAndParseToken(token);
        return getRole(claimsJws);
    }

    /**
     * Gets the role of the user from a given JWT token.
     * Assumes that the provided token is valid (check this with 'validate' method).
     *
     * @param claimsJws  the claims from a valid parsed token
     * @return the role from JWT (STUDENT, CANDIDATE_TA, TA, LECTURER, ADMIN)
     */
    public String getRole(Jws<Claims> claimsJws) {
        return jwtUtils.getRole(claimsJws);
    }

    /**
     * Gets the authentication of a JWT (used in JwtFilter).
     *
     * @param token the JWT to get the authentication from.
     * @return the Authentication object made from the data in the JWT.
     *          Null if the token is invalid
     */
    public Authentication getAuthentication(String token) {
        Jws<Claims> claims = this.validateAndParseToken(token);
        if (claims == null) {
            return null;
        }
        return this.getAuthentication(claims);
    }

    /**
     * Gets the authentication of a JWT (used in JwtFilter).
     *
     * @param claimsJws the claims from a valid parsed token
     * @return the Authentication object made from the data in the JWT.
     */
    public Authentication getAuthentication(Jws<Claims> claimsJws) {
        long userId = Long.parseLong(this.getSubject(claimsJws));

        UserDetails userDetails = this.authService.loadUserByUserId(userId);

        return new UsernamePasswordAuthenticationToken(userDetails,
                "", userDetails.getAuthorities());
    }

    /**
     * Gets the username of a user whose userId is in the JWT token.
     *
     * @param token the JWT to get the authentication from.
     * @return the username of the user,
     *         null if the token is invalid
     */
    public String getUsername(String token) {
        Jws<Claims> claims = this.validateAndParseToken(token);
        if (claims == null) {
            return null;
        }
        long userId = Long.parseLong(this.getSubject(claims));

        return this.authService.loadUserByUserId(userId).getUsername();
    }
}

