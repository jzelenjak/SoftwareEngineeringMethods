package nl.tudelft.sem.authentication.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.security.Key;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import nl.tudelft.sem.authentication.security.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {
    @Value("${application.jwt.secretKey:" +
            "securesecuresecuresecuresecuresecuresecuresecuresecuresecuresecure}")
    private String secretKey;

    @Value("${application.jwt.tokenExpirationAfterMinutes:15}")
    private long validityInMinutes;

    @Autowired
    private UserDetailsService userDetailsService;

    /**
     * Create a new JWT.
     *
     * @param username username of the user.
     * @param role     role of the user.
     * @return JWT.
     */
    public String createToken(String username, UserRole role) {
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("role", role);
        Date now = new Date();
        Date validity = new Date(now.getTime() + TimeUnit.MILLISECONDS.toSeconds(validityInMinutes));
        Key hmacKey = new SecretKeySpec(secretKey.getBytes(),
                SignatureAlgorithm.HS256.getJcaName());
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(hmacKey)
                .compact();
    }

    /**
     * Get the authentication of a JWT.
     *
     * @param token the JWT to get the authentication from.
     * @return the authentication in the JWT.
     */
    public Authentication getAuthentication(String token) {
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(
                this.getUsername(token)
        );
        return new UsernamePasswordAuthenticationToken(
                userDetails,
                "",
                userDetails.getAuthorities()
        );
    }

    /**
     * Get the username from a JWT.
     *
     * @param token JWT to get the username from.
     * @return the username in the JWT.
     */
    public String getUsername(String token) {
        return Jwts.parserBuilder().setSigningKey(secretKey).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    /**
     * Get a JWT from a request's Authorization header.
     *
     * @param req The request to get the JWT from.
     * @return The JWT in the request. Null if no JWT was found.
     */
    public String resolveToken(HttpServletRequest req) {
        String bearerToken = req.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7, bearerToken.length());
        }
        return null;
    }

    /**
     * Validate a JWT.
     *
     * @param token JWT to validate.
     * @return True if the token is valid, false otherwise.
     */
    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(secretKey).build()
                    .parseClaimsJws(token);
            if (claims.getBody().getExpiration().before(new Date())) {
                return false;
            }
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            throw new IllegalArgumentException("Expired or invalid JWT token");
        }
    }

    /**
     * Gets user details service.
     *
     * @return user details service.
     */
    public UserDetailsService getUserDetailsService() {
        return this.userDetailsService;
    }

    /**
     * Sets user details service.
     *
     * @param userDetailsService the user details service.
     */
    public void setUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    /**
     * Gets secret key.
     *
     * @return secret key.
     */
    public String getSecretKey() {
        return this.secretKey;
    }

    /**
     * Sets secret key.
     *
     * @param secretKey the secret key.
     */
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    /**
     * Gets validity in minutes.
     *
     * @return validity in minutes.
     */
    public long getValidityInMinutes() {
        return this.validityInMinutes;
    }

    /**
     * Sets validity in minutes.
     *
     * @param validityInMinutes the validity in minutes.
     */
    public void setValidityInMinutes(long validityInMinutes) {
        this.validityInMinutes = validityInMinutes;
    }
}

