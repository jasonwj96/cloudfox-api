package com.cloudfox.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/cloudfox-api/v1/accounts/register",
                                "/cloudfox-api/v1/session/login",
                                "/cloudfox-api/v1/session/validate",
                                "/cloudfox-api/v1/model/find/id",
                                "/cloudfox-api/v1/model/find/accountid",
                                "/cloudfox-api/v1/model/create",
                                "/cloudfox-api/v1/model/find-by-id",
                                "/cloudfox-api/v1/model/find-by-account",
                                "/cloudfox-api/v1/payment/intent"

                        ).permitAll()
                        .anyRequest().authenticated()
                )

                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
