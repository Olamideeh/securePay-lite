package com.example.securepay.config;

import com.example.securepay.security.ApiKeyAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;

    public SecurityConfig(
            ApiKeyAuthenticationFilter apiKeyAuthenticationFilter
    ) {
        this.apiKeyAuthenticationFilter = apiKeyAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/merchants/register",
                                "/actuator/health",
                                "/error"
                        ).permitAll()

                        .requestMatchers("/api/v1/customers/**")
                        .hasRole("MERCHANT")

                        .anyRequest()
                        .authenticated()
                )

                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(
                                (request, response, exception) ->
                                        response.sendError(
                                                HttpStatus.UNAUTHORIZED.value(),
                                                "A valid API key is required"
                                        )
                        )
                )

                .addFilterBefore(
                        apiKeyAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}