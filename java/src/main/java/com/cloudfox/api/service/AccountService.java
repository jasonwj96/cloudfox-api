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
import java.util.UUID;

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

    public AccountResponse getAccountById(UUID accountId) {
        AccountResponse response = new AccountResponse();
        Optional<Account> account = accountRepository.findById(accountId);

        if (account.isPresent()) {
            response = AccountResponse.builder()
                    .username(account.get().getUsername())
                    .email(account.get().getEmail())
                    .fullname(account.get().getFullname())
                    .tokenBalance(account.get().getTokenBalance())
                    .pricingPlanMicros(account.get().getPricingPlan().getPriceMicros())
                    .pricingPlanCurrency(account.get().getPricingPlan().getCurrency().getCode())
                    .build();
        }

        return response;
    }
}