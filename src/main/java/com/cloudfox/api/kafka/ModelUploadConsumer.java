package com.cloudfox.api.kafka;

import com.cloudfox.api.enums.ModelStatus;
import com.cloudfox.api.model.Model;
import com.cloudfox.api.repository.ModelRepository;
import com.cloudfox.api.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class ModelUploadConsumer {

    private final ModelRepository modelRepository;
    private final S3Service s3Service;

    @KafkaListener(topics = "model-upload-topic", groupId = "model-upload-group")
    @Transactional
    public void consume(ModelUploadEvent event) {

        Model model = modelRepository.findById(event.getModelId())
                .orElseThrow();

        try {

            model.setStatus(ModelStatus.PROCESSING);
            modelRepository.save(model);

            Path path = Paths.get(event.getTempFilePath());

            try (InputStream stream = Files.newInputStream(path)) {

                s3Service.uploadFile(
                        event.getS3Key(),
                        "application/octet-stream",
                        Files.size(path),
                        stream
                );

            }

            model.setStatus(ModelStatus.COMPLETED);

        } catch (Exception e) {
            model.setStatus(ModelStatus.FAILED);
        }

        modelRepository.save(model);
    }
}