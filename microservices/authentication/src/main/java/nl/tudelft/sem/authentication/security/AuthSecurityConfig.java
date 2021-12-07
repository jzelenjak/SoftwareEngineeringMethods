package nl.tudelft.sem.authentication.security;

import nl.tudelft.sem.authentication.jwt.JwtConfig;
import nl.tudelft.sem.authentication.jwt.JwtTokenProvider;
import nl.tudelft.sem.authentication.service.AuthService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;


/**
 * A class for security configuration.
 */
@Configuration
@EnableWebSecurity
public class AuthSecurityConfig extends WebSecurityConfigurerAdapter {
    private final transient AuthService authService;
    private final transient PasswordEncoder passwordEncoder;
    private final transient JwtTokenProvider jwtTokenProvider;

    /**
     * Instantiates a new AuthSecurityConfig object.
     */
    public AuthSecurityConfig(AuthService authService, PasswordEncoder passwordEncoder,
                              JwtTokenProvider jwtTokenProvider) {
        this.authService = authService;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
                .antMatchers(HttpMethod.GET, "/api/auth/login").permitAll()
                .antMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                .antMatchers(HttpMethod.PUT, "/api/auth/change_password").authenticated()
                .antMatchers(HttpMethod.PUT, "/api/auth/change_role")
                    .hasAnyAuthority("ADMIN", "LECTURER")
                .antMatchers(HttpMethod.PUT, "/api/auth/delete")
                    .hasAnyAuthority("ADMIN")
                .anyRequest()
                .authenticated()
            .and()
            .apply(new JwtConfig(this.jwtTokenProvider));
    }


    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
            .userDetailsService(this.authService)
            .passwordEncoder(this.passwordEncoder);
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}

