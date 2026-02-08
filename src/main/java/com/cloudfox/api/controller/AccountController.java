package com.cloudfox.api.controller;

import com.cloudfox.api.dto.request.AccountRequest;
import com.cloudfox.api.dto.response.AccountResponse;
import com.cloudfox.api.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/register")
    public ResponseEntity<AccountResponse> registerAccount(@RequestBody @Valid AccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(accountService.createAccount(request));
    }

    @GetMapping("/profile")
    public ResponseEntity<AccountResponse> getProfile(
            @AuthenticationPrincipal UUID accountId) {
        return ResponseEntity.ok(accountService.getAccountById(accountId));
    }
}