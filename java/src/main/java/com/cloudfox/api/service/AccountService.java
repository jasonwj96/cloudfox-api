package com.cloudfox.api.service;

import com.cloudfox.api.dto.request.AccountRequest;
import com.cloudfox.api.dto.response.AccountResponse;
import com.cloudfox.api.exceptions.AccountAlreadyExists;
import com.cloudfox.api.model.Account;
import com.cloudfox.api.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AuthenticationService authenticationService;

    public AccountResponse createAccount(AccountRequest accountRequest) {
        if (accountRepository.existsByUsername(accountRequest.getUsername())) {
            throw new AccountAlreadyExists();
        }

        AuthenticationService.Argon2HashResult hashResult =
                authenticationService.argon2Hash(accountRequest.getPassword());

        Account createdAccount = accountRepository.save(Account.builder()
                .username(accountRequest.getUsername())
                .fullname(accountRequest.getFullname())
                .email(accountRequest.getEmail())
                .passwordHash(hashResult.hash())
                .passwordHashAlgo(hashResult.algorithm())
                .active(true)
                .build());

        // 4. Map to response
        return AccountResponse.builder()
                .username(createdAccount.getUsername())
                .fullname(createdAccount.getFullname())
                .email(createdAccount.getEmail())
                .build();
    }
}