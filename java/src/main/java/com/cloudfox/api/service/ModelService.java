package com.cloudfox.api.service;

import com.cloudfox.api.dto.request.ModelRequest;
import com.cloudfox.api.dto.response.ModelDTO;
import com.cloudfox.api.dto.response.ModelResponse;
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

@Service
@RequiredArgsConstructor
public class ModelService {

    private final ModelRepository modelRepository;
    private final SessionRepository sessionRepository;
    private final S3Service s3Service;
    private final AccountRepository accountRepository;

    @Transactional
    public ModelResponse createModel(ModelRequest request) {
        ModelResponse response = new ModelResponse();

        Optional<LoginSession> session = sessionRepository
                .findBySessionTokenAndIsActiveTrueAndExpirationDateAfter(
                        request.getSessionToken(), Instant.now());

        if (session.isPresent()) {
            String s3Key = session.get().getAccountId() + "/" + request.getFileName();

            s3Service.saveFile(request.getFilePayload(), s3Key);

            Model newModel = Model.builder()
                    .accountId(session.get().getAccountId())
                    .name(request.getModelName())
                    .fileName(request.getFileName())
                    .framework(request.getFramework())
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

    public ModelResponse getAccountModel(ModelRequest request) {
        ModelResponse response = new ModelResponse();

        Optional<LoginSession> session = sessionRepository
                .findBySessionTokenAndIsActiveTrueAndExpirationDateAfter(
                        request.getSessionToken(), Instant.now());

        if (session.isPresent()) {
            Optional<Account> account = accountRepository.findById(session.get().getAccountId());
            if (account.isPresent()) {
                Model model = modelRepository.findModelByIdAndAccountId(
                        request.getModelId(), account.get().getId());

                ModelDTO modelDTO = ModelDTO.builder()
                        .id(model.getId())
                        .accountId(model.getAccountId())
                        .name(model.getName())
                        .generatedTokens(model.getGeneratedTokens())
                        .creationDate(model.getCreationDate())
                        .fileName(model.getFileName())
                        .framework(model.getFramework())
                        .active(model.isActive())
                        .build();

                response.setModel(modelDTO);
            }
        }

        return response;
    }


    public ModelResponse getAccountModels(ModelRequest request) {
        ModelResponse response = new ModelResponse();

        Optional<LoginSession> session = sessionRepository
                .findBySessionTokenAndIsActiveTrueAndExpirationDateAfter(
                        request.getSessionToken(), Instant.now());

        if (session.isPresent()) {
            Optional<Account> account = accountRepository.findById(session.get().getAccountId());
            if (account.isPresent()) {
                List<Model> models = modelRepository
                        .findModelByAccountId(account.get().getId());

                List<ModelDTO> modelDTOList = models.stream()
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
                                .build()
                        )
                        .toList();

                response.setModels(modelDTOList);
            }
        }

        return response;
    }
}