package nl.tudelft.sem.authentication.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.util.Date;


/**
 * A class for JTW configuration.
 */
@Configuration
@ConfigurationProperties(prefix = "application.jwt")
public class JwtTokenUtil {
    private String secretKey;
    private String tokenPrefix;
    private Integer tokenExpirationAfterMinutes;

    /**
     * Instantiates a new JWT configuration object.
     */
    public JwtTokenUtil() {
    }

    public String generateToken(Authentication auth) {
        return Jwts.builder()
                .setSubject(auth.getName())
                .claim("authorities", auth.getAuthorities())
                .setIssuedAt(new Date())
                .setExpiration(java.sql.Date.valueOf(LocalDate.now()
                        .plusDays(tokenExpirationAfterMinutes)))
                .signWith(Keys.hmacShaKeyFor(this.secretKey.getBytes()))
                .compact();
    }

    /**
     * Gets the secret key for the JWT Token.
     *
     * @return the secret key for the JWT Token.
     */
    public String getSecretKey() {
        return this.secretKey;
    }

    /**
     * Sets the secret key for the JWT Token.
     *
     * @param secretKey the secret key for the JWT Token.
     */
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    /**
     * Gets the token prefix for the JWT token.
     *
     * @return the token prefix for the JWT token.
     */
    public String getTokenPrefix() {
        return this.tokenPrefix;
    }

    /**
     * Sets token prefix for the JWT token.
     *
     * @param tokenPrefix the token prefix for the JWT token.
     */
    public void setTokenPrefix(String tokenPrefix) {
        this.tokenPrefix = tokenPrefix;
    }

    /**
     * Gets the token expiration time (after minutes).
     *
     * @return the token expiration time (after minutes).
     */
    public Integer getTokenExpirationAfterMinutes() {
        return this.tokenExpirationAfterMinutes;
    }

    /**
     * Sets the token expiration time (after minutes).
     *
     * @param tokenExpirationAfterMinutes the token expiration time (after minutes)
     */
    public void setTokenExpirationAfterMinutes(Integer tokenExpirationAfterMinutes) {
        this.tokenExpirationAfterMinutes = tokenExpirationAfterMinutes;
    }
}
