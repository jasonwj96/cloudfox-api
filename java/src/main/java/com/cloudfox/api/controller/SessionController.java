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
            @RequestBody @Valid SessionRequest request,
            HttpServletResponse response
    ) {
        LoginSession session = sessionService.createSession(request);

        ResponseCookie cookie = ResponseCookie.from("SESSION", session.getSessionToken().toString())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Strict")
                .maxAge(30 * 24 * 60 * 60)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(
                SessionResponse.builder()
                        .expirationDate(session.getExpirationDate())
                        .build()
        );
    }


    @PostMapping("/validate")
    public ResponseEntity<SessionResponse> refreshSession(
            @CookieValue("SESSION") UUID sessionToken
    ) {
        SessionResponse response = sessionService.refreshSession(
                SessionRequest.builder()
                        .sessionToken(sessionToken)
                        .build()
        );

        return ResponseEntity.ok(response);
    }
}