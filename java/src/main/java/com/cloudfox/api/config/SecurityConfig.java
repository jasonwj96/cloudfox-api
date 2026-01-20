package com.cloudfox.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST,
                                "/cloudfox-api/v1/accounts/register",
                                "/cloudfox-api/v1/session/login",
                                "/cloudfox-api/v1/payment/intent")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/cloudfox-api/v1/session/get-account-by-session")
                        .permitAll()
                        .anyRequest()
                        .authenticated()
                );

        return http.build();
    }

}
