package com.cloudfox.api.auth;

import com.cloudfox.api.service.SessionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@RequiredArgsConstructor
public class SessionAuthenticationFilter extends OncePerRequestFilter {

    private final SessionService sessionService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            Cookie[] cookies = request.getCookies();

            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("__host_cfx_sid".equals(cookie.getName())) {

                        UUID accountId = sessionService
                                .findValidSession(UUID.fromString(cookie.getValue()))
                                .getAccountId();

                        if (accountId != null) {
                            SecurityContextHolder.getContext()
                                    .setAuthentication(new SessionAuthentication(accountId));
                        }

                        break;
                    }
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}