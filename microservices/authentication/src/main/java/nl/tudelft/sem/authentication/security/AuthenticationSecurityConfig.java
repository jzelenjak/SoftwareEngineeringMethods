package nl.tudelft.sem.authentication.security;

import javax.crypto.SecretKey;
import nl.tudelft.sem.authentication.auth.AuthService;
import nl.tudelft.sem.authentication.jwt.JwtTokenUtil;
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


@Configuration
@EnableWebSecurity
public class AuthenticationSecurityConfig extends WebSecurityConfigurerAdapter {
    private final transient AuthService authService;
    private final transient PasswordEncoder passwordEncoder;

    /**
     * Configuration for the authentication security.
     */
    public AuthenticationSecurityConfig(AuthService authService, PasswordEncoder passwordEncoder) {
        this.authService = authService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
            // Disable CSRF
            .csrf().disable()
            // Set session management to stateless
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            // Set permissions on endpoints
            .authorizeRequests()
                // Our public endpoints
                .antMatchers(HttpMethod.POST, "/auth/register").permitAll()
                .antMatchers( "/auth/login").permitAll()
                .antMatchers(HttpMethod.PUT, "/auth/change_password").permitAll()
                .antMatchers(HttpMethod.GET, "/auth/attempt").permitAll()
                // Our private endpoints
                .anyRequest()
                .authenticated();
    }


    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .userDetailsService(this.authService)
                .passwordEncoder(this.passwordEncoder);
    }

    @Override @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}

