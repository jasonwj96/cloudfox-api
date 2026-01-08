package com.cloudfox.api.controller;

import com.cloudfox.api.dto.request.AccountRequest;
import com.cloudfox.api.dto.response.AccountResponse;
import com.cloudfox.api.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cloudfox-api/v1/accounts")
@RequiredArgsConstructor
public class AccountsController {

    private final AccountService accountService;

    @PostMapping("/register")
    public ResponseEntity<AccountResponse> registerAccount(@RequestBody @Valid AccountRequest request) {
        AccountResponse response = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}