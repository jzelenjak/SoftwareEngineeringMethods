package nl.tudelft.sem.authentication.security;

import nl.tudelft.sem.authentication.auth.AuthenticationService;
import nl.tudelft.sem.authentication.jwt.JwtConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.crypto.SecretKey;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class AuthenticationSecurityConfig extends WebSecurityConfigurerAdapter {
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationService authenticationService;
    private final JwtConfig jwtConfig;
    //private final SecretKey secretKey;

    @Autowired
    public AuthenticationSecurityConfig(PasswordEncoder passwordEncoder,
                                        AuthenticationService authenticationService,
                                        JwtConfig jwtConfig//,
                                        /*SecretKey secretKey*/) {
        this.passwordEncoder = passwordEncoder;
        this.authenticationService = authenticationService;
        this.jwtConfig = jwtConfig;
        //this.secretKey = secretKey;
    }

    @Override
    public void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf().disable()
                .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                //.addFilter(new JwtUsernameAndPasswordAuthenticationFilter(authenticationManager(), jwtConfig, secretKey))
                //.addFilterAfter(new JwtTokenVerifier(secretKey, jwtConfig),JwtUsernameAndPasswordAuthenticationFilter.class)
                .authorizeRequests()
                .antMatchers("/", "index").permitAll()
                .anyRequest()
                .authenticated();
    }

    @Override
    public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        //authenticationManagerBuilder.authenticationProvider(/*dao*/);
    }

    /*
    Add DAO authentication provider
     */
}

