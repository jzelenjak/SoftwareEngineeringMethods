package nl.tudelft.sem.authentication.jwt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Date;
import javax.crypto.SecretKey;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


/**
 * A class for filtering credentials (username and password).
 */
public class CredentialsFilter extends UsernamePasswordAuthenticationFilter {
    private final transient AuthenticationManager authenticationManager;
    private final transient JwtTokenUtil jwtTokenUtil;
    private final transient SecretKey secretKey;

    /**
     * Instantiates a new Credentials filter.
     *
     * @param authenticationManager the authentication manager
     * @param jwtTokenUtil             the JWT configuration
     * @param secretKey             the secret key for the JWT
     */
    public CredentialsFilter(AuthenticationManager authenticationManager, JwtTokenUtil jwtTokenUtil, SecretKey secretKey) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.secretKey = secretKey;
    }

    /**
     * Gets the authentication manager.
     *
     * @return the authentication manager.
     */
    @Override
    public AuthenticationManager getAuthenticationManager() {
        return this.authenticationManager;
    }

    /**
     * Attempts to authenticate the HTTP request.
     *
     * @param request HTTP request
     * @param response HTTP response
     *
     * @return the token for an authentication request.
     * @throws AuthenticationException when Authentication object is invalid.
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            JsonNode jsonNode = new ObjectMapper().readTree(request.getInputStream());
            Authentication auth = new UsernamePasswordAuthenticationToken(jsonNode.get("username").asText(),
                                                                            jsonNode.get("password").asText());
            return authenticationManager.authenticate(auth);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Issues the JWT after successful authentication.
     *
     * @param request HTTP request
     * @param response HTTP response
     * @param chain the filter chain
     * @param authResult the result of the authentication
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authResult) {
        String token = jwtTokenUtil.generateToken(authResult);
        response.addHeader(HttpHeaders.AUTHORIZATION, jwtTokenUtil.getTokenPrefix() + token);
    }
}
