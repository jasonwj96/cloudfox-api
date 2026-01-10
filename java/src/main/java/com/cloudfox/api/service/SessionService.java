package com.cloudfox.api.service;

import com.cloudfox.api.dto.request.SessionRequest;
import com.cloudfox.api.dto.response.SessionResponse;
import com.cloudfox.api.enums.HashAlgorithm;
import com.cloudfox.api.exceptions.AuthenticationException;
import com.cloudfox.api.exceptions.SessionNotCreated;
import com.cloudfox.api.model.Account;
import com.cloudfox.api.model.LoginSession;
import com.cloudfox.api.repository.AccountRepository;
import com.cloudfox.api.repository.SessionsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionsRepository sessionsRepository;
    private final AccountRepository accountRepository;
    private final CryptoService cryptoService;

    public SessionResponse createSession(SessionRequest request) {
        Account account = accountRepository.findByUsername(request.getUsername())
                .orElseThrow(AuthenticationException::new);

        HashAlgorithm algorithm;

        try {
            algorithm = HashAlgorithm.valueOf(account.getPasswordHashAlgo().toUpperCase());
        } catch (Exception e) {
            throw new IllegalStateException("Unsupported hash algorithm");
        }

        boolean hashIsValid = switch (algorithm) {
            case ARGON2 -> cryptoService.verifyArgon2Hash(
                    account.getPasswordHash(),
                    request.getPassword()
            );
            case BCRYPT -> cryptoService.verifyBcryptHash(
                    account.getPasswordHash(),
                    request.getPassword()
            );
        };

        if (!hashIsValid) {
            throw new AuthenticationException();
        }

        LoginSession session = LoginSession.builder()
                .accountId(account.getId())
                .userAgent(request.getUserAgent())
                .ipAddress(request.getIpAddress())
                .expirationDate(Instant.now().plus(30, ChronoUnit.DAYS))
                .isActive(true)
                .build();

        try {
            sessionsRepository.save(session);
        } catch (Exception e) {
            throw new SessionNotCreated();
        }

        return new SessionResponse(session.getSessionToken());
    }
}