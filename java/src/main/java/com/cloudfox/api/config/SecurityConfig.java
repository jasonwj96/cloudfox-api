package com.cloudfox.api.config;

import com.cloudfox.api.auth.SessionAuthenticationFilter;
import com.cloudfox.api.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final SessionService sessionService;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(
                        new SessionAuthenticationFilter(sessionService),
                        UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST,
                                "/cloudfox-api/v1/accounts/register",
                                "/cloudfox-api/v1/session/login",
                                "/cloudfox-api/v1/session/logout",
                                "/cloudfox-api/v1/payment/stripe/webhook")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/cloudfox-api/v1/session/get-account-by-session",
                                "/cloudfox-api/v1/model/**")
                        .permitAll()
                        .anyRequest()
                        .authenticated()
                );

        return http.build();
    }
}