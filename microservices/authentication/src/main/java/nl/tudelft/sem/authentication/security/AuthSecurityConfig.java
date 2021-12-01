package nl.tudelft.sem.authentication.security;

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

    /**
     * Instantiates the security configuration class.
     */
    public AuthSecurityConfig(AuthService authService, PasswordEncoder passwordEncoder) {
        this.authService = authService;
        this.passwordEncoder = passwordEncoder;
    }

    /** Configures HTTP security (requests, permissions, authentication, csrf etc)
     * @param http HTTP security object
     * @throws Exception if something goes wrong
     */
    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
                .antMatchers(HttpMethod.GET, "/auth/login").permitAll()
                .antMatchers(HttpMethod.POST, "/auth/register").permitAll()
                .antMatchers(HttpMethod.PUT, "/auth/change_password").permitAll()
                .anyRequest()
                .authenticated();
    }


    /**
     * Configures authentication manager using AuthenticationManagerBuilder
     * @param auth authentication manager builder
     * @throws Exception if something goes wrong
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
            .userDetailsService(this.authService)
            .passwordEncoder(this.passwordEncoder);
    }

    /**
     * Creates an authentication manager bean
     * @return the authentication manager bean
     * @throws Exception if something goes wrong
     */
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}

