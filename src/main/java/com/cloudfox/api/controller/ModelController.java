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
@RequestMapping("/model")
@RequiredArgsConstructor
public class ModelController {

    private final ModelService modelService;

    @GetMapping("/find-by-account")
    public ResponseEntity<ModelResponse> findByAccountId(
            @AuthenticationPrincipal UUID accountId) {
        return ResponseEntity.ok(
                modelService.getAccountModels(accountId)
        );
    }

    @GetMapping()
    public ResponseEntity<ModelResponse> findAll() {
        return ResponseEntity.ok(modelService.getAllModels());
    }

    @PostMapping(
            value = "/create",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ModelResponse> createModel(
            @AuthenticationPrincipal UUID accountId,
            @ModelAttribute @Valid ModelRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(modelService.createModel(accountId, request));
    }

    @PostMapping("/find-by-id")
    public ResponseEntity<ModelResponse> findById(
            @RequestBody ModelRequest request,
            @AuthenticationPrincipal UUID accountId) {
        return ResponseEntity.ok(
                modelService.getAccountModel(accountId, request)
        );
    }

    @PostMapping("/save")
    public ResponseEntity<ModelResponse> saveModel(
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