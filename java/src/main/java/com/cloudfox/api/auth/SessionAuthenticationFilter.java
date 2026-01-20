package com.cloudfox.api.auth;

import com.cloudfox.api.service.SessionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

public class SessionAuthenticationFilter extends OncePerRequestFilter {

    private final SessionService sessionService;

    public SessionAuthenticationFilter(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("SESSION".equals(cookie.getName())) {
                        try {
                            UUID token = UUID.fromString(cookie.getValue());
                            UUID accountId = sessionService.findValidSession(token).getAccountId();

                            if (accountId != null) {
                                SessionAuthentication auth =
                                        new SessionAuthentication(accountId);
                                SecurityContextHolder.getContext()
                                        .setAuthentication(auth);
                            }
                        } catch (IllegalArgumentException ignored) {

                        }
                        break;
                    }
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
