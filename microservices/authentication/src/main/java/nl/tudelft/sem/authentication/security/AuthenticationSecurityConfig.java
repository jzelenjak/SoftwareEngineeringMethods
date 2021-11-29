package nl.tudelft.sem.authentication.security;

import javax.crypto.SecretKey;
import nl.tudelft.sem.authentication.jwt.CredentialsFilter;
import nl.tudelft.sem.authentication.jwt.JwtConfig;
import nl.tudelft.sem.authentication.jwt.JwtTokenVerifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class AuthenticationSecurityConfig extends WebSecurityConfigurerAdapter {
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;
    private final SecretKey secretKey;
    private final JwtConfig jwtConfig;

    /**
     * Configuration for the authentication security.
     *
     * @param passwordEncoder       the password encoder.
     * @param userDetailsService    the user detail service.
     * @param secretKey             the secret key of the JWT token.
     * @param jwtConfig             the configuration of the JWT token.
     */
    @Autowired
    public AuthenticationSecurityConfig(PasswordEncoder passwordEncoder,
                                        UserDetailsService userDetailsService,
                                        SecretKey secretKey, JwtConfig jwtConfig) {
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
        this.secretKey = secretKey;
        this.jwtConfig = jwtConfig;
    }

    @Override
    public void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf().disable()
                .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .addFilter(new CredentialsFilter(authenticationManager(),
                        jwtConfig, this.secretKey))
                .addFilterAfter(new JwtTokenVerifier(secretKey, jwtConfig),
                        CredentialsFilter.class)
                .authorizeRequests()
                .antMatchers("/", "index").permitAll()
                .anyRequest()
                .authenticated();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authProvider());
    }

    /**
     * Configure the authentication provider.
     *
     * @return The authentication provider as DaoAuthenticationProvider.
     */
    @Bean
    public DaoAuthenticationProvider authProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(this.userDetailsService);
        authProvider.setPasswordEncoder(this.passwordEncoder);
        return authProvider;
    }

    /**
     * Gets the password encoder.
     *
     * @return The password encoder as PasswordEncoder.
     */
    @Bean
    public PasswordEncoder getPasswordEncoder() {
        return this.passwordEncoder;
    }

    /**
     * Gets the user details service.
     *
     * @return The user details service as UserDetailsService.
     */
    @Bean
    public UserDetailsService getUserDetailsService() {
        return this.userDetailsService;
    }

    /**
     * Gets the secret key.
     *
     * @return The secret key as SecretKey.
     */
    @Bean
    public SecretKey getSecretKey() {
        return this.secretKey;
    }

    /**
     * Gets the Jwt configuration.
     *
     * @return The Jwt configuration as JwtConfig.
     */
    @Bean
    public JwtConfig getJwtConfig() {
        return this.jwtConfig;
    }
}

