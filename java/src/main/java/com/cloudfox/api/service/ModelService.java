package com.cloudfox.api.service;

import com.cloudfox.api.dto.request.ModelRequest;
import com.cloudfox.api.dto.response.ModelDTO;
import com.cloudfox.api.dto.response.ModelResponse;
import com.cloudfox.api.exceptions.InvalidSessionToken;
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

        String s3Key = session.getAccountId() + "/" + request.getFileName();
        s3Service.saveFile(request.getFilePayload(), s3Key);

        Model model = Model.builder()
                .accountId(session.getAccountId())
                .name(request.getModelName())
                .fileName(request.getFileName())
                .framework(request.getFramework())
                .active(true)
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
                .accountId(model.getAccountId())
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

        List<ModelDTO> models = modelRepository
                .findModelByAccountId(session.getAccountId())
                .stream()
                .map(model -> ModelDTO.builder()
                        .id(model.getId())
                        .accountId(model.getAccountId())
                        .name(model.getName())
                        .generatedTokens(model.getGeneratedTokens())
                        .creationDate(model.getCreationDate())
                        .fileName(model.getFileName())
                        .framework(model.getFramework())
                        .active(model.isActive())
                        .lastModified(model.getLastModified())
                        .build())
                .toList();

        return ModelResponse.builder()
                .models(models)
                .build();
    }
}