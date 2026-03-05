package com.cloudfox.api.dto.response;

public record ApiError(
        String timestamp,
        int status,
        String error,
        String message,
        String code
) {}