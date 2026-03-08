package com.cloudfox.api.controller;

import com.cloudfox.api.dto.request.ModelRequest;
import com.cloudfox.api.dto.response.ModelResponse;
import com.cloudfox.api.service.ModelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/models")
@RequiredArgsConstructor
public class ModelController {

    private final ModelService modelService;

    @GetMapping("/{modelId}")
    public ResponseEntity<ModelResponse> getById(
            @AuthenticationPrincipal UUID accountId,
            @PathVariable UUID modelId) {
        return ResponseEntity.ok(
                modelService.getModel(accountId, modelId)
        );
    }

    @GetMapping()
    public ResponseEntity<ModelResponse> getAll() {
        return ResponseEntity.ok(modelService.getAllModels());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ModelResponse> createModel(
            @AuthenticationPrincipal UUID accountId,
            @ModelAttribute @Valid ModelRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(modelService.createModel(accountId, request));
    }

    @PatchMapping
    public ResponseEntity<ModelResponse> updateModel(
            @AuthenticationPrincipal UUID accountId,
            @ModelAttribute @Valid ModelRequest request) {
        int rowsAffected = modelService.saveModel(accountId, request);

        return rowsAffected > 0
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{modelId}")
    public ResponseEntity<Void> deleteById(
            @AuthenticationPrincipal UUID accountId,
            @PathVariable UUID modelId) {

        int rowsAffected = modelService.deleteModel(accountId, modelId);

        return rowsAffected > 0
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}