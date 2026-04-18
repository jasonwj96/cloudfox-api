package com.cloudfox.api.service;

import com.cloudfox.api.dto.request.ModelRequest;
import com.cloudfox.api.dto.response.ModelDTO;
import com.cloudfox.api.dto.response.ModelParam;
import com.cloudfox.api.dto.response.ModelResponse;
import com.cloudfox.api.dto.response.XgbModelFile;
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
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class ModelService {

    private final ModelRepository modelRepository;
    private final AccountRepository accountRepository;
    private final ModelKafkaProducer modelKafkaProducer;
    private final S3Service s3Service;
    private final ObjectMapper objectMapper;

    @Transactional
    public ModelResponse createModel(UUID accountId, ModelRequest request) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));

        try {

            Path tempFile = Files.createTempFile(
                    "model-",
                    request.getFilePayload().getOriginalFilename()
            );

            request.getFilePayload().transferTo(tempFile);

            Model model = Model.builder()
                    .account(account)
                    .name(request.getModelName())
                    .fileName(request.getFilePayload().getOriginalFilename())
                    .framework(request.getFramework())
                    .status(ModelStatus.PENDING)
                    .build();

            model = modelRepository.save(model);

            String s3Key =
                    accountId + "/" +
                            model.getId() + "/" +
                            request.getFilePayload().getOriginalFilename();

            ModelUploadEvent event = ModelUploadEvent.builder()
                    .modelId(model.getId())
                    .accountId(accountId)
                    .tempFilePath(tempFile.toString())
                    .s3Key(s3Key)
                    .framework(request.getFramework())
                    .build();

            modelKafkaProducer.sendModelUploadEvent(event);

            return ModelResponse.builder()
                    .name(model.getName())
                    .fileName(model.getFileName())
                    .build();

        } catch (IOException e) {
            throw new RuntimeException("Failed to store uploaded model file", e);
        }
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
                .status(model.getStatus())
                .build();

        return ModelResponse.builder()
                .model(dto)
                .build();
    }

    public ModelResponse getAccountModels(UUID accountId) {
        List<ModelDTO> models = modelRepository.findByAccountId(accountId)
                .stream()
                .map(model -> {
                    try (InputStream file = s3Service.getFile(accountId, model.getId(), model.getFileName())) {
                        XgbModelFile modelFile = objectMapper.readValue(file, XgbModelFile.class);
                        List<String> featureNames = modelFile.learner().featureNames();
                        List<String> featureTypes = modelFile.learner().featureTypes();

                        List<ModelParam> modelParams = IntStream.range(0, featureNames.size())
                                .mapToObj(i -> ModelParam.builder()
                                        .featureName(featureNames.get(i))
                                        .dataType(featureTypes.get(i))
                                        .build())
                                .toList();
                        return ModelDTO.builder()
                                .id(model.getId())
                                .accountName(model.getAccount().getUsername())
                                .name(model.getName())
                                .generatedTokens(model.getGeneratedTokens())
                                .creationDate(model.getCreationDate())
                                .fileName(model.getFileName())
                                .framework(model.getFramework())
                                .status(model.getStatus())
                                .lastModified(model.getLastModified())
                                .modelParams(modelParams)
                                .build();
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to parse model artifact for model: " + model.getId(), e);
                    }
                })
                .toList();

        return ModelResponse.builder()
                .models(models)
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
                        .status(model.getStatus())
                        .lastModified(model.getLastModified())
                        .build()).toList();

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