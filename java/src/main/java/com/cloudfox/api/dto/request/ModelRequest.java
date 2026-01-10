package com.cloudfox.api.dto.request;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ModelRequest {
    private UUID sessionToken;
    private UUID modelId;
    private String accountId;
    private String modelName;
    private String fileName;
    private boolean modelStatus;
}