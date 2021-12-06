package nl.tudelft.sem.authentication.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import java.security.Key;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import nl.tudelft.sem.authentication.security.UserRole;
import nl.tudelft.sem.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

/**
 * A class that provides utilities related to JWT token.
 */
@Component
public class JwtTokenProvider {

    private final transient Key hmacKey;

    @Value("${jwtTokenValidityInMinutes:10}")
    private transient long validityInMinutes;

    @Autowired
    private transient UserDetailsService userDetailsService;

    private transient JwtUtils jwtUtils;

    /**
     * Instantiates JwtTokenProvider object.
     *
     * @param hmacKey   The secret key used to sign the token
     */
    public JwtTokenProvider(@Qualifier("secretKey") Key hmacKey, JwtUtils jwtUtils) {
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
        String bearerToken = req.getHeader("Authorization");
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
        Jws<Claims> claimsJws = jwtUtils.validateAndParseClaims(token);
        return claimsJws != null;
//        try {
//            Jwts
//                .parserBuilder()
//                .setSigningKey(this.hmacKey)
//                .build()
//                .parseClaimsJws(token);
//            return true;
//        } catch (Exception e) {
//            return false;
//        }
    }

//    /**
//     * Gets the username from the user with a given JWT token.
//     *
//     * @param token the JWT to get the username from.
//     * @return the username from the JWT.
//     */
//    public String getUsername(String token) {
//        // TODO: change if we need user ID?
//        return Jwts.parserBuilder()
//                .setSigningKey(this.hmacKey)
//                .build()
//                .parseClaimsJws(token)
//                .getBody()
//                .getSubject();
//    }
//
//    /**
//     * Gets the role from the user with the given JWT token.
//     * Assumes that the provided token is valid (check this with 'validate' method).
//     *
//     * @param token the JWT token (assumed to be valid)
//     * @return the role from JWT (STUDENT, LECTURER, TA, ADMIN)
//     */
//    public String getRole(String token) {
//        Jws<Claims> claimsJws = jwtUtils.validateAndParseClaims(token);
//        String role = jwtUtils.getRole(claimsJws);
//        return role;
//    }

    /**
     * Gets the username from the user with a given JWT token.
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
     * Gets the role from the user with the given JWT token.
     * Assumes that the provided token is valid (check this with 'validate' method).
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

    /**
     * Gets the authentication of a JWT.
     *
     * @param token the JWT to get the authentication from.
     * @return the authentication in the JWT.
     */
    public Authentication getAuthentication(String token) {
        if (!validateToken(token)) {
            return null;
        }
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(
                this.getUsername(token));
        return new UsernamePasswordAuthenticationToken(userDetails,
                "", userDetails.getAuthorities());
    }
}

