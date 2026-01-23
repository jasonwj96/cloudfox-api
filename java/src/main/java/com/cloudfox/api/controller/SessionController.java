package com.cloudfox.api.controller;

import com.cloudfox.api.dto.request.SessionRequest;
import com.cloudfox.api.dto.response.SessionResponse;
import com.cloudfox.api.model.LoginSession;
import com.cloudfox.api.service.SessionService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.hc.core5.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/cloudfox-api/v1/session")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @PostMapping("/login")
    public ResponseEntity<SessionResponse> loginAccount(
            @CookieValue(name = "SESSION", required = false) UUID sessionToken,
            @RequestBody @Valid SessionRequest request,
            HttpServletResponse response) {
        LoginSession session;

        if (sessionToken == null) {
            session = sessionService.createSession(request);

            ResponseCookie cookie = ResponseCookie.from("SESSION", session.getSessionToken().toString())
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .sameSite("Lax")
                    .maxAge(30 * 24 * 60 * 60)
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        } else {
            session = sessionService.findValidSession(sessionToken);
        }

        return ResponseEntity.ok(
                SessionResponse.builder()
                        .expirationDate(session.getExpirationDate())
                        .build()
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logoutAccount(
            @CookieValue(name = "SESSION", required = false) UUID sessionToken,
            HttpServletResponse response) {

        if (sessionToken != null) {
            LoginSession session = sessionService.findValidSession(sessionToken);
            if (session != null) {
                sessionService.invalidateSession(sessionToken);
            }

            ResponseCookie cookie = ResponseCookie.from("SESSION", "")
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .sameSite("None")
                    .maxAge(0)
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        }

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/get-account-by-session")
    public ResponseEntity<SessionResponse> refreshSession(
            @CookieValue("SESSION") UUID sessionToken) {
        SessionResponse response = sessionService.getAccountBySession(sessionToken);
        return ResponseEntity.ok(response);
    }
}