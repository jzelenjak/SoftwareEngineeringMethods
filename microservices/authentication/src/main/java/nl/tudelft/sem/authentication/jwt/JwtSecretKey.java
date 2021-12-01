package nl.tudelft.sem.authentication.jwt;

import io.jsonwebtoken.SignatureAlgorithm;
import java.security.Key;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for JWT token key.
 */
@Configuration
public class JwtSecretKey {
    @Value("${jwtSecretKeyString}")
    private transient String secretKeyString;

    /**
     * Creates the secret key for JWT token.
     *
     * @return the secret key
     */
    @Bean("secretKey")
    public Key secretKey() {
        return new SecretKeySpec(this.secretKeyString.getBytes(),
                SignatureAlgorithm.HS256.getJcaName());
    }
}

