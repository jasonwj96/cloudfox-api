package com.cloudfox.api.service;

import com.cloudfox.api.model.Account;
import com.cloudfox.api.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AuthenticationService authenticationService;

    public Account createAccount(String username, String fullname, String password) {

        AuthenticationService.Argon2HashResult hashResult = authenticationService.argon2Hash(password);

        Account account = new Account();
        account.setUsername(username);
        account.setFullname(fullname);
        account.setEmail(null);
        account.setPasswordHash(hashResult.hash());
        account.setPasswordHashAlgo(hashResult.algorithm());
        account.setActive(true);

        return accountRepository.save(account);
    }
}
