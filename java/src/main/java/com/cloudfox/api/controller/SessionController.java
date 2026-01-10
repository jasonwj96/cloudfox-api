package com.cloudfox.api.controller;

import com.cloudfox.api.dto.request.SessionRequest;
import com.cloudfox.api.dto.response.SessionResponse;
import com.cloudfox.api.service.SessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cloudfox-api/v1/session")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @PostMapping("/login")
    public ResponseEntity<SessionResponse> loginAccount(@RequestBody @Valid SessionRequest request) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(sessionService.createSession(request));
    }

    @PostMapping("/validate")
    public ResponseEntity<SessionResponse> refreshSession(@RequestBody @Valid SessionRequest request){
        return ResponseEntity.status(HttpStatus.OK)
                .body(sessionService.refreshSession(request));
    }
}