package com.cloudfox.api.repository;

import com.cloudfox.api.model.LoginSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SessionsRepository extends JpaRepository<LoginSession, UUID> {

    Optional<LoginSession> findBySessionTokenAndIsActiveTrueAndExpirationDateAfter(
            UUID sessionToken,
            Instant now
    );

    List<LoginSession> findByAccountIdAndIsActiveTrue(UUID accountId);

    boolean existsByAccountIdAndIsActiveTrue(UUID accountId);

    @Modifying
    @Query("""
                UPDATE LoginSession s
                SET s.isActive = false
                WHERE s.sessionToken = :token
            """)
    void deactivateByToken(UUID token);

    @Modifying
    @Query("""
                UPDATE LoginSession s
                SET s.isActive = false
                WHERE s.accountId = :accountId
            """)
    void deactivateAllByAccountId(UUID accountId);

    @Modifying
    @Query("""
                UPDATE LoginSession s
                SET s.lastActiveDate = :now,
                s.expirationDate = :expirationDate
                WHERE s.sessionToken = :token
                  AND s.isActive = true
            """)
    void refreshExpirationDate(UUID token, Instant now, Instant expirationDate);
}