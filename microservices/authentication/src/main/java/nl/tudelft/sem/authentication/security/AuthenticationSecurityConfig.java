package nl.tudelft.sem.authentication.security;

//import nl.tudelft.sem.authentication.jwt.JwtConfig;
import nl.tudelft.sem.authentication.jwt.JwtConfig;
        import nl.tudelft.sem.authentication.jwt.JwtTokenVerifier;
import nl.tudelft.sem.authentication.jwt.CredentialsFilter;

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

import javax.crypto.SecretKey;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class AuthenticationSecurityConfig extends WebSecurityConfigurerAdapter {
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;
    private final JwtConfig jwtConfig;
    private final SecretKey secretKey;

    @Autowired
    public AuthenticationSecurityConfig(PasswordEncoder passwordEncoder,
                                        UserDetailsService userDetailsService,
                                        JwtConfig jwtConfig,
                                        SecretKey secretKey) {
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
        this.jwtConfig = jwtConfig;
        this.secretKey = secretKey;
    }

    @Override
    public void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf().disable()
                .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .addFilter(new CredentialsFilter(authenticationManager(), jwtConfig, this.secretKey))
                .addFilterAfter(new JwtTokenVerifier(secretKey, jwtConfig), CredentialsFilter.class)
                .authorizeRequests()
                .antMatchers("/", "index").permitAll()
                .anyRequest()
                .authenticated();
//                .and().apply(new JwtConfigurer(jwtTokenProvider));
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authProvider());
    }

    /**
     * Configure the authentication provider.
     *
     * @return DaoAuthenticationProvider The authentication provider.
     */
    @Bean
    public DaoAuthenticationProvider authProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(this.userDetailsService);
        authProvider.setPasswordEncoder(this.passwordEncoder);
        return authProvider;
    }
}

