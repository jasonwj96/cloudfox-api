package com.cloudfox.api.service;

import com.cloudfox.api.dto.request.ModelRequest;
import com.cloudfox.api.dto.response.ModelResponse;
import com.cloudfox.api.model.LoginSession;
import com.cloudfox.api.model.Model;
import com.cloudfox.api.repository.ModelRepository;
import com.cloudfox.api.repository.SessionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ModelService {

    private final ModelRepository modelRepository;
    private final SessionRepository sessionRepository;

    @Transactional
    public ModelResponse createModel(ModelRequest request) {
        ModelResponse response = new ModelResponse();

        Optional<LoginSession> session = sessionRepository
                .findBySessionTokenAndIsActiveTrueAndExpirationDateAfter(
                        request.getSessionToken(), Instant.now());

        if (session.isPresent()) {
            Model newModel = Model.builder()
                    .accountId(session.get().getAccountId())
                    .name(request.getModelName())
                    .fileName(request.getFileName())
                    .active(true)
                    .build();

            newModel = modelRepository.save(newModel);
            response.setName(newModel.getName());
            response.setFileName(newModel.getFileName());
        } else {
            throw new SessionAuthenticationException("Session does not exist");
        }

        return response;
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
}