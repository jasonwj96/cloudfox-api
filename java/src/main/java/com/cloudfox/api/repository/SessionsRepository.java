package com.cloudfox.api.repository;

import com.cloudfox.api.model.LoginSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SessionsRepository extends JpaRepository<LoginSession, UUID> {

    LoginSession findBySessionTokenAndIsActiveIsTrue(UUID sessionToken);

    List<LoginSession> findByAccountIdAndIsActiveTrue(UUID accountId);

    boolean existsBySessionToken(UUID sessionToken);

    boolean existsByAccountId(UUID accountId);

}
