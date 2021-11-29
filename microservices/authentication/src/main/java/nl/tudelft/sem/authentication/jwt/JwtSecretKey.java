package nl.tudelft.sem.authentication.jwt;

import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * A class for JWT Secret Key.
 * Spring will create the Secret Key automatically
 * based on the properties in the application.properties.
 */
@Configuration
public class JwtSecretKey {
    private final JwtConfig jwtConfig;

    /**
     * Instantiates a new JWT secret key.
     *
     * @param jwtConfig the JWT config
     */
    @Autowired
    public JwtSecretKey(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    /**
     * Gets the JWT configuration object to which this secret key is related to.
     *
     * @return the JWT configuration object
     */
    public JwtConfig getJwtConfig() {
        return this.jwtConfig;
    }

    /**
     * Creates the secret key for JWT token.
     *
     * @return the secret key
     */
    @Bean
    public SecretKey secretKey() {
        return Keys.hmacShaKeyFor(jwtConfig.getSecretKey().getBytes());
    }
}
