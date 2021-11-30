package nl.tudelft.sem.authentication.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;


/**
 * The class for verifying JWT token.
 */
public class JwtTokenFilter extends OncePerRequestFilter {
    private transient final SecretKey secretKey;
    private transient final JwtTokenUtil jwtTokenUtil;

    /**
     * Instantiates a new JWT token verifier.
     *
     * @param secretKey the secret key for the JWT token
     * @param jwtTokenUtil the configuration for the JWT token
     */
    public JwtTokenFilter(SecretKey secretKey, JwtTokenUtil jwtTokenUtil) {
        this.secretKey = secretKey;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    /**
     * Performs the internal filtering once per each request.
     *
     * @param request HTTP request
     * @param response HTTP response
     * @param filterChain the filter chain
     * @throws ServletException when the servlet encounters difficulty
     * @throws IOException when an I/O exception has occurred
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader == null || authorizationHeader.isEmpty()
                || !authorizationHeader.startsWith(jwtTokenUtil.getTokenPrefix())) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.replace(jwtTokenUtil.getTokenPrefix(), "");


        Jws<Claims> claimsJws = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token);

        Claims body = claimsJws.getBody();
        String username = body.getSubject();
        List<Map<String, String>> authorities =
                (List<Map<String, String>>) body.get("authorities");

        Set<SimpleGrantedAuthority> simpleGrantedAuthorities = authorities.stream()
                .map(m -> new SimpleGrantedAuthority(m.get("authority")))
                .collect(Collectors.toSet());

        Authentication authentication = new UsernamePasswordAuthenticationToken(username,
                null, simpleGrantedAuthorities);


        SecurityContextHolder.getContext().setAuthentication(authentication);


        filterChain.doFilter(request, response);
    }
}
