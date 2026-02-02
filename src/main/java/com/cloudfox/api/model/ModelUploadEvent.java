package com.cloudfox.api.model;

import java.util.UUID;

public record ModelUploadEvent(
        UUID modelId,
        UUID accountId,
        String fileName,
        byte[] payload,
        String framework
) {
}