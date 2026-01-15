package com.cloudfox.api.dto.response;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ModelDTO {
    private UUID id;
    private UUID accountId;
    private String name;
    private int generatedTokens;
    private Instant creationDate;
    private String fileName;
    private String framework;
    private boolean active;
    private Instant lastModified;
}
