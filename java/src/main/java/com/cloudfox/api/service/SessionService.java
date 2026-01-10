package com.cloudfox.api.service;

import com.cloudfox.api.dto.request.SessionRequest;
import com.cloudfox.api.dto.response.SessionResponse;
import com.cloudfox.api.enums.HashAlgorithm;
import com.cloudfox.api.exceptions.AuthenticationException;
import com.cloudfox.api.exceptions.InvalidSessionToken;
import com.cloudfox.api.exceptions.SessionNotCreated;
import com.cloudfox.api.model.Account;
import com.cloudfox.api.model.LoginSession;
import com.cloudfox.api.repository.AccountRepository;
import com.cloudfox.api.repository.SessionsRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hibernate.query.common.TemporalUnit;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.UUID;

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
        SessionResponse response = new SessionResponse();

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
                .sessionToken(UUID.randomUUID())
                .userAgent(request.getUserAgent())
                .ipAddress(request.getIpAddress())
                .creationDate(Instant.now())
                .expirationDate(Instant.now().plus(30, ChronoUnit.DAYS))
                .isActive(true)
                .build();

        try {
            LoginSession newSession = sessionsRepository.save(session);
            response.setSessionToken(newSession.getSessionToken());
        } catch (Exception e) {
            throw new SessionNotCreated();
        }

        return response;
    }

    @Transactional
    public SessionResponse refreshSession(@Valid SessionRequest request) {
        sessionsRepository.refreshExpirationDate(request.getSessionToken(),
                Instant.now(),
                Instant.now().plus(30, ChronoUnit.DAYS));

        LoginSession session = sessionsRepository
                .findBySessionTokenAndIsActiveTrueAndExpirationDateAfter(
                        request.getSessionToken(), Instant.now())
                .orElseThrow(InvalidSessionToken::new);

        return SessionResponse.builder()
                .sessionToken(session.getSessionToken())
                .expirationDate(session.getExpirationDate())
                .build();
    }
}