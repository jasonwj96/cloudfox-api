package com.cloudfox.api.controller;

import com.cloudfox.api.dto.request.AccountRequest;
import com.cloudfox.api.dto.response.AccountResponse;
import com.cloudfox.api.model.Account;
import com.cloudfox.api.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cloudfox-api/v1/accounts")
public class AccountsController {

    @Autowired
    private  AccountService accountService;


    @PostMapping("/register")
    public ResponseEntity<AccountResponse> registerAccount(
            @RequestBody AccountRequest request
    ) {
        Account account = accountService.createAccount(
                request.getUsername(),
                request.getFullname(),
                request.getPassword()
        );

        AccountResponse response = new AccountResponse();
        response.setUsername(account.getUsername());


        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
