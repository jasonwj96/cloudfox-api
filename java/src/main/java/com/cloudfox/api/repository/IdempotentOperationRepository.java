package com.cloudfox.api.repository;

import com.cloudfox.api.model.IdempotentOperation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface IdempotentOperationRepository
        extends JpaRepository<IdempotentOperation, Long> {

    Optional<IdempotentOperation>
    findByIdempotencyKeyAndOperation(String idempotencyKey, String operation);

    void deleteByExpiresAtBefore(OffsetDateTime now);
}