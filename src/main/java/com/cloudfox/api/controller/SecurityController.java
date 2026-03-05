package com.cloudfox.api.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/security")
@RequiredArgsConstructor
public class SecurityController {

    @GetMapping("csrf-token")
    public CsrfToken getCSRFToken(CsrfToken token) {
        return token;
    }
}