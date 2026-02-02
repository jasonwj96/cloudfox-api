package com.cloudfox.api.repository;

import com.cloudfox.api.dto.response.ModelDTO;
import com.cloudfox.api.model.Model;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ModelRepository extends JpaRepository<Model, UUID> {
    Model findModelByIdAndAccountId(UUID id, UUID accountId);

    @Query("""
                select new com.cloudfox.api.dto.response.ModelDTO(
                    m.id,
                    m.account.username,
                    m.name,
                    m.generatedTokens,
                    m.creationDate,
                    m.fileName,
                    m.framework,
                    m.active,
                    m.lastModified
                )
                from Model m
                where m.account.id = :accountId
            """)
    List<ModelDTO> findModelsWithAccountName(UUID accountId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                UPDATE Model m
                SET m.generatedTokens = m.generatedTokens + :amount,
                    m.lastModified = CURRENT_TIMESTAMP
                WHERE m.id = :modelId
            """)
    int incrementGeneratedTokens(UUID modelId, int amount);

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
        SET m.active = :modelStatus,
            m.name = :modelName,
            m.lastModified = CURRENT_TIMESTAMP
        WHERE m.id = :modelId
          AND m.account.id = :accountId
       """)
    int updateModelStatusAndName(
            UUID modelId,
            UUID accountId,
            String modelName,
            Boolean modelStatus
    );
}