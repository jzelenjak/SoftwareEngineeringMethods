package nl.tudelft.sem.authentication.jwt;

import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;

@Configuration
public class JwtSecretKey {
    private final transient JwtTokenUtil jwtTokenUtil;

    /**
     * Instantiates a new JWT secret key.
     *
     * @param jwtTokenUtil the JWT config
     */
    @Autowired
    public JwtSecretKey(JwtTokenUtil jwtTokenUtil) {
        this.jwtTokenUtil = jwtTokenUtil;
    }

    /**
     * Creates the secret key for JWT token.
     *
     * @return the secret key
     */
    @Bean
    public SecretKey secretKey() {
        return Keys.hmacShaKeyFor(jwtTokenUtil.getSecretKey().getBytes());
    }
}

