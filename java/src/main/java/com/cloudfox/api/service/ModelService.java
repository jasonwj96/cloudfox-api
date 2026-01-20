package com.cloudfox.api.service;

import com.cloudfox.api.dto.request.ModelRequest;
import com.cloudfox.api.dto.response.ModelDTO;
import com.cloudfox.api.dto.response.ModelResponse;
import com.cloudfox.api.exceptions.InvalidSessionToken;
import com.cloudfox.api.model.Account;
import com.cloudfox.api.model.LoginSession;
import com.cloudfox.api.model.Model;
import com.cloudfox.api.repository.AccountRepository;
import com.cloudfox.api.repository.ModelRepository;
import com.cloudfox.api.repository.SessionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ModelService {

    private final ModelRepository modelRepository;
    private final SessionRepository sessionRepository;
    private final S3Service s3Service;
    private final AccountRepository accountRepository;

    @Transactional
    public ModelResponse createModel(UUID sessionToken, ModelRequest request) {

        LoginSession session = sessionRepository
                .findBySessionTokenAndIsActiveTrueAndExpirationDateAfter(
                        sessionToken, Instant.now())
                .orElseThrow(() ->
                        new SessionAuthenticationException("Invalid session"));

        Account account = accountRepository.findById(session.getAccountId())
                .orElseThrow(() ->
                        new EntityNotFoundException("Account not found"));

        String s3Key = account.getId() + "/" + request.getFilePayload().getOriginalFilename();
        s3Service.saveFile(request.getFilePayload(), s3Key);

        Model model = Model.builder()
                .account(account)
                .name(request.getModelName())
                .fileName(request.getFilePayload().getOriginalFilename())
                .framework(request.getFramework())
                .active(request.getModelStatus())
                .build();

        model = modelRepository.save(model);

        return ModelResponse.builder()
                .name(model.getName())
                .fileName(model.getFileName())
                .build();
    }

    @Transactional
    public ModelResponse addGeneratedTokens(ModelRequest modelRequest) {
        ModelResponse response = new ModelResponse();
        int gainedTokens = 10;

        int updated = modelRepository.incrementGeneratedTokens(modelRequest.getModelId(), gainedTokens);

        if (updated != 1) {
            throw new EntityNotFoundException("Model not found: " + modelRequest.getModelId());
        }

        return response;
    }

    public ModelResponse getAccountModel(UUID sessionToken, ModelRequest request) {

        LoginSession session = sessionRepository
                .findBySessionTokenAndIsActiveTrueAndExpirationDateAfter(
                        sessionToken, Instant.now())
                .orElseThrow(InvalidSessionToken::new);

        Model model = modelRepository.findModelByIdAndAccountId(
                request.getModelId(),
                session.getAccountId()
        );

        ModelDTO dto = ModelDTO.builder()
                .id(model.getId())
                .accountName(model.getAccount().getUsername())
                .name(model.getName())
                .generatedTokens(model.getGeneratedTokens())
                .creationDate(model.getCreationDate())
                .fileName(model.getFileName())
                .framework(model.getFramework())
                .active(model.isActive())
                .build();

        return ModelResponse.builder()
                .model(dto)
                .build();
    }

    public ModelResponse getAccountModels(UUID sessionToken) {

        LoginSession session = sessionRepository
                .findBySessionTokenAndIsActiveTrueAndExpirationDateAfter(
                        sessionToken, Instant.now())
                .orElseThrow(InvalidSessionToken::new);

        return ModelResponse.builder()
                .models(
                        modelRepository.findModelsWithAccountName(
                                session.getAccountId()
                        )
                )
                .build();
    }

    public int deleteModel(UUID sessionToken, UUID modelId) {
        LoginSession session = sessionRepository
                .findBySessionTokenAndIsActiveTrueAndExpirationDateAfter(
                        sessionToken, Instant.now())
                .orElseThrow(InvalidSessionToken::new);

        return modelRepository.deleteByIdAndAccountId(
                modelId,
                session.getAccountId()
        );
    }
}