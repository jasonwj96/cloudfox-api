package com.cloudfox.api.service;

import com.cloudfox.api.dto.request.SessionRequest;
import com.cloudfox.api.dto.response.SessionResponse;
import com.cloudfox.api.enums.HashAlgorithm;
import com.cloudfox.api.exceptions.AuthenticationException;
import com.cloudfox.api.exceptions.InvalidSessionToken;
import com.cloudfox.api.model.Account;
import com.cloudfox.api.model.LoginSession;
import com.cloudfox.api.repository.AccountRepository;
import com.cloudfox.api.repository.SessionRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;
    private final AccountRepository accountRepository;
    private final CryptoService cryptoService;

    public LoginSession createSession(SessionRequest request) {
        Account account = accountRepository.findByUsername(request.getUsername())
                .orElseThrow(AuthenticationException::new);

        HashAlgorithm algorithm = HashAlgorithm.valueOf(
                account.getPasswordHashAlgo().toUpperCase()
        );

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
                .sessionToken(UUID.randomUUID())
                .userAgent(request.getUserAgent())
                .ipAddress(request.getIpAddress())
                .creationDate(Instant.now())
                .expirationDate(Instant.now().plus(30, ChronoUnit.DAYS))
                .isActive(true)
                .build();

        return sessionRepository.save(session);
    }

    public LoginSession findValidSession(UUID sessionToken) {
        return sessionRepository
                .findBySessionTokenAndIsActiveTrueAndExpirationDateAfter(
                        sessionToken,
                        Instant.now()
                )
                .orElse(null);
    }

    @Transactional
    public void refresh(UUID sessionToken) {
        sessionRepository.refreshExpirationDate(sessionToken, Instant.now(),
                Instant.now().plus(30, ChronoUnit.DAYS));
    }

    public SessionResponse getAccountBySession(UUID sessionToken) {
        SessionResponse response = new SessionResponse();
        return response;
    }

    @Transactional
    public void invalidateSession(UUID sessionToken) {
        sessionRepository.deleteSessionById(sessionToken);
    }
}