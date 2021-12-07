package nl.tudelft.sem.authentication.jwt;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

/**
 * A class that acts as a filter for validating JWT token.
 */
public class JwtTokenFilter extends GenericFilterBean {

    private final transient JwtTokenProvider jwtTokenProvider;

    /**
     * Instantiates a new JWT token filter object.
     *
     * @param jwtTokenProvider the JWT token provider with various methods related to JWT.
     */
    public JwtTokenFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain)
            throws IOException, ServletException {
        // Extract and parse the token
        String token = jwtTokenProvider.resolveToken((HttpServletRequest) req);
        Jws<Claims> claims = jwtTokenProvider.validateAndParseToken(token);

        if (claims != null) {
            // Make the user to be authenticated
            Authentication auth = jwtTokenProvider.getAuthentication(claims);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        filterChain.doFilter(req, res);
    }
}
