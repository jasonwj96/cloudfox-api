package com.cloudfox.api.service;

import com.cloudfox.api.dto.request.ModelRequest;
import com.cloudfox.api.dto.response.ModelDTO;
import com.cloudfox.api.dto.response.ModelResponse;
import com.cloudfox.api.enums.ModelStatus;
import com.cloudfox.api.exceptions.ModelDoesNotExist;
import com.cloudfox.api.kafka.ModelKafkaProducer;
import com.cloudfox.api.kafka.ModelUploadEvent;
import com.cloudfox.api.model.Account;
import com.cloudfox.api.model.Model;
import com.cloudfox.api.repository.AccountRepository;
import com.cloudfox.api.repository.ModelRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ModelService {

    private final ModelRepository modelRepository;
    private final AccountRepository accountRepository;
    private final ModelKafkaProducer modelKafkaProducer;

    @Transactional
    public ModelResponse createModel(UUID accountId, ModelRequest request) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));

        String s3Key = accountId + "/" + request.getFilePayload().getOriginalFilename();

        Model model = Model.builder()
                .account(account)
                .name(request.getModelName())
                .fileName(request.getFilePayload().getOriginalFilename())
                .framework(request.getFramework())
                .status(ModelStatus.PENDING)
                .build();

        model = modelRepository.save(model);

        ModelUploadEvent event = ModelUploadEvent.builder()
                .modelId(model.getId())
                .accountId(accountId)
                .tempFilePath(request.getFilePayload().getOriginalFilename())
                .s3Key(s3Key)
                .framework(request.getFramework())
                .build();

        modelKafkaProducer.sendModelUploadEvent(event);

        return ModelResponse.builder()
                .name(model.getName())
                .fileName(model.getFileName())
                .build();
    }

    public ModelResponse getModel(UUID accountId, UUID modelId) {

        Model model = modelRepository.findByIdAndAccountId(
                modelId,
                accountId).orElseThrow(ModelDoesNotExist::new);

        ModelDTO dto = ModelDTO.builder()
                .id(model.getId())
                .accountName(model.getAccount().getUsername())
                .name(model.getName())
                .generatedTokens(model.getGeneratedTokens())
                .creationDate(model.getCreationDate())
                .fileName(model.getFileName())
                .framework(model.getFramework())
                .status(model.getStatus().toString())
                .build();

        return ModelResponse.builder()
                .model(dto)
                .build();
    }

    public ModelResponse getAccountModels(UUID accountId) {
        return ModelResponse.builder()
                .models(modelRepository.findModelsWithAccountName(accountId))
                .build();
    }

    public ModelResponse getAllModels() {
        List<ModelDTO> models = modelRepository.findAll().stream().map(model ->
                ModelDTO.builder()
                        .id(model.getId())
                        .accountName(model.getAccount().getUsername())
                        .name(model.getName())
                        .generatedTokens(model.getGeneratedTokens())
                        .creationDate(model.getCreationDate())
                        .fileName(model.getFileName())
                        .framework(model.getFramework())
                        .status(model.getStatus().toString())
                        .lastModified(model.getLastModified())
                        .build()
        ).toList();

        return ModelResponse.builder()
                .models(models)
                .build();
    }

    @Transactional
    public int deleteModel(UUID accountId, UUID modelId) {
        return modelRepository.deleteByIdAndAccountId(
                modelId,
                accountId
        );
    }

    @Transactional
    public int saveModel(UUID accountId, UUID modelId, @Valid ModelRequest request) {
        return modelRepository.updateModelStatusAndName(
                modelId,
                accountId,
                request.getModelName(),
                request.getModelStatus()
        );
    }


}