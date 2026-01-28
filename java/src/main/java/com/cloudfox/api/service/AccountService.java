package com.cloudfox.api.service;

import com.cloudfox.api.dto.request.AccountRequest;
import com.cloudfox.api.dto.response.AccountResponse;
import com.cloudfox.api.enums.PricingPlanEnum;
import com.cloudfox.api.exceptions.AccountAlreadyExists;
import com.cloudfox.api.model.Account;
import com.cloudfox.api.model.PricingPlan;
import com.cloudfox.api.repository.AccountRepository;
import com.cloudfox.api.repository.PrincingPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final CryptoService authenticationService;
    private final PrincingPlanRepository princingPlanRepository;

    public AccountResponse createAccount(AccountRequest accountRequest) {
        if (accountRepository.existsByUsername(accountRequest.getUsername())) {
            throw new AccountAlreadyExists();
        }

        CryptoService.Argon2HashResult hashResult =
                authenticationService.argon2Hash(accountRequest.getPassword());


        PricingPlan plan = princingPlanRepository.findPricingPlanByCode(
                PricingPlanEnum.FREE_PLAN.getValue()
        );

        Account createdAccount = accountRepository.save(Account.builder()
                .username(accountRequest.getUsername())
                .fullname(accountRequest.getFullname())
                .email(accountRequest.getEmail())
                .passwordHash(hashResult.hash())
                .passwordHashAlgo(hashResult.algorithm())
                .pricingPlan(plan)
                .active(true)
                .build());

        return AccountResponse.builder()
                .username(createdAccount.getUsername())
                .fullname(createdAccount.getFullname())
                .email(createdAccount.getEmail())
                .build();
    }

    public Optional<Account> getAccountByUsername(String username) {
        return accountRepository.findByUsername(username);
    }
}