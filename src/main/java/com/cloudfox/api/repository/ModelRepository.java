package com.cloudfox.api.repository;

import com.cloudfox.api.enums.ModelStatus;
import com.cloudfox.api.model.Model;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ModelRepository extends JpaRepository<Model, UUID> {
    Optional<Model> findByIdAndAccountId(UUID id, UUID accountId);

    @Query("from Model m join fetch m.account where m.account.id = :accountId")
    List<Model> findByAccountId(@Param("accountId") UUID accountId);

    @Modifying
    @Query("""
                DELETE FROM Model m
                WHERE m.id = :modelId
                  AND m.account.id = :accountId
            """)
    int deleteByIdAndAccountId(UUID modelId, UUID accountId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Model m
            SET m.status = :modelStatus,
                m.name = :modelName,
                m.lastModified = CURRENT_TIMESTAMP
            WHERE m.id = :modelId
              AND m.account.id = :accountId
            """)
    int updateModelStatusAndName(
            @Param("modelId") UUID modelId,
            @Param("accountId") UUID accountId,
            @Param("modelName") String modelName,
            @Param("modelStatus") ModelStatus modelStatus
    );
}