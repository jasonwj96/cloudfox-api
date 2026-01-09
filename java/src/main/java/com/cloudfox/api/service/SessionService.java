package com.cloudfox.api.service;

import com.cloudfox.api.model.LoginSession;
import com.cloudfox.api.repository.SessionsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionsRepository sessionsRepository;

    public LoginSession createSession(UUID accountId, String userAgent,
                                      String ipAddress, Instant expirationDate) {
        LoginSession session = LoginSession.builder()
                .accountId(accountId)
                .userAgent(userAgent)
                .ipAddress(ipAddress)
                .expirationDate(expirationDate)
                .isActive(true)
                .build();

        return sessionsRepository.save(session);
    }

    public LoginSession findSessionByToken(UUID sessionToken) {
        return sessionsRepository.findBySessionTokenAndIsActiveIsTrue(sessionToken);
    }

    public List<LoginSession> findSessionByAccountId(UUID accountId) {
        return sessionsRepository.findByAccountIdAndIsActiveTrue(accountId);
    }

    public boolean accountSessionExists(UUID accountId) {
        return sessionsRepository.existsByAccountId(accountId);
    }

    public boolean sessionTokenExists(UUID sessionToken) {
        return sessionsRepository.existsBySessionToken(sessionToken);
    }
}