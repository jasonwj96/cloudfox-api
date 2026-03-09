package com.cloudfox.api.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ModelKafkaProducer {

    private final KafkaTemplate<String, ModelUploadEvent> kafkaTemplate;

    public void sendModelUploadEvent(ModelUploadEvent event) {
        kafkaTemplate.send("model-upload-topic", event.getModelId().toString(), event);
    }

}