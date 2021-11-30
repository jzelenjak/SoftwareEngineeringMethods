package nl.tudelft.sem.authentication.security;

import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletResponse;

import nl.tudelft.sem.authentication.auth.AuthService;
import nl.tudelft.sem.authentication.jwt.CredentialsFilter;
import nl.tudelft.sem.authentication.jwt.JwtTokenFilter;
import nl.tudelft.sem.authentication.jwt.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
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
//@EnableGlobalMethodSecurity(prePostEnabled = true)
public class AuthenticationSecurityConfig extends WebSecurityConfigurerAdapter {
    private final transient AuthService authService;
    private final transient PasswordEncoder passwordEncoder;
    private final transient JwtTokenUtil jwtTokenUtil;
    private final transient SecretKey secretKey;

    /**
     * Configuration for the authentication security.
     */
    public AuthenticationSecurityConfig(AuthService authService, PasswordEncoder passwordEncoder,
                                        SecretKey secretKey, JwtTokenUtil jwtTokenUtil) {
        this.authService = authService;
        this.passwordEncoder = passwordEncoder;
        this.secretKey = secretKey;
        this.jwtTokenUtil = jwtTokenUtil;
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
            // Set unauthorized requests exception handler
//            .exceptionHandling()
//                .authenticationEntryPoint((request, response, ex) ->
//                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage()))
//            .and()
            .addFilter(new CredentialsFilter(authenticationManager(), jwtTokenUtil, this.secretKey))
//            .addFilterAfter(new JwtTokenFilter(secretKey, jwtTokenUtil), CredentialsFilter.class)
            // Set permissions on endpoints
            .authorizeRequests()
                // Our public endpoints
                .antMatchers(HttpMethod.POST, "/auth/register").permitAll()
                .antMatchers( "/auth/login").permitAll()
                // Our private endpoints
                .anyRequest()
                .authenticated();

        //.addFilterBefore(new JwtTokenFilter(secretKey, jwtConfig), CredentialsFilter.class);
    }


    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception{
//        auth.authenticationProvider(authProvider());
        auth.userDetailsService(this.authService);
    }

    @Override @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }


//    /**
//     * Configure the authentication provider.
//     *
//     * @return The authentication provider as DaoAuthenticationProvider.
//     */
//
//    public DaoAuthenticationProvider authProvider() {
//        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
//        authProvider.setUserDetailsService(this.authService);
//        authProvider.setPasswordEncoder(this.passwordEncoder);
//        return authProvider;
//    }


//    @Bean
//    public UserDetailsService getUserDetailsService() {
//        return this.authService;
//    }
}

