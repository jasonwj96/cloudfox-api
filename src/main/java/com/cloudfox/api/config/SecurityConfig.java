package com.cloudfox.api.config;

import com.cloudfox.api.auth.SessionAuthenticationFilter;
import com.cloudfox.api.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final SessionService sessionService;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) {

        CookieCsrfTokenRepository cookieRepo = CookieCsrfTokenRepository.withHttpOnlyFalse();
        cookieRepo.setCookiePath("/");

        http
                .cors(AbstractHttpConfigurer::disable)
                .csrf(csrf -> csrf
                        .csrfTokenRepository(cookieRepo)
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                        .ignoringRequestMatchers("/payment/stripe/webhook")
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new SessionAuthenticationFilter(sessionService),
                        AnonymousAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(HttpMethod.GET, "/auth/csrf")
                        .permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/accounts/register",
                                "/session/login",
                                "/session/logout")
                        .permitAll()
                        .anyRequest()
                        .authenticated()
                );

        return http.build();
    }
}