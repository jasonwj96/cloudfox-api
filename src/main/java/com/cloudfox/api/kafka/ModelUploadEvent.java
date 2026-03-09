package com.cloudfox.api.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ModelUploadEvent {

    private UUID modelId;
    private UUID accountId;
    private String tempFilePath;
    private String s3Key;
    private String framework;

}