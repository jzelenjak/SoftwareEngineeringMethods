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

import nl.tudelft.sem.authentication.auth.AuthService;
import nl.tudelft.sem.authentication.security.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {
    @Value("${application.jwt.secretKey:" +
            "securesecuresecuresecuresecuresecuresecuresecuresecuresecuresecure}")
    private transient String secretKey;

    @Value("${application.jwt.tokenExpirationAfterMinutes:15}")
    private transient long validityInMinutes;

    @Autowired
    private transient AuthService authService;

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
        UserDetails userDetails = this.authService.loadUserByUsername(this.getUsername(token));
        return new UsernamePasswordAuthenticationToken(userDetails,"", userDetails.getAuthorities());
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
            return bearerToken.substring(7);
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
            return !claims.getBody().getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            throw new IllegalArgumentException("Expired or invalid JWT token");
        }
    }
}

