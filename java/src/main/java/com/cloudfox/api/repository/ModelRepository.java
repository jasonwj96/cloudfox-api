package com.cloudfox.api.repository;

import com.cloudfox.api.model.Model;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ModelRepository extends JpaRepository<Model, UUID> {
    Model findModelByIdAndAccountId(UUID id, UUID accountId);

    List<Model> findModelByAccountId(UUID accountId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                UPDATE Model m
                SET m.generatedTokens = m.generatedTokens + :amount,
                    m.lastModified = CURRENT_TIMESTAMP
                WHERE m.id = :modelId
            """)
    int incrementGeneratedTokens(UUID modelId, int amount);
}