package com.cloudfox.api.dto.request;

import com.cloudfox.api.enums.ModelStatus;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ModelRequest {
    private UUID modelId;
    private String modelName;
    private String framework;
    private MultipartFile filePayload;
    private ModelStatus modelStatus;
}