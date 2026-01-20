package com.cloudfox.api.controller;

import com.cloudfox.api.dto.request.ModelRequest;
import com.cloudfox.api.dto.response.ModelResponse;
import com.cloudfox.api.service.ModelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/cloudfox-api/v1/model")
@RequiredArgsConstructor
public class ModelController {

    private final ModelService modelService;

    @PostMapping(
            value = "/create",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ModelResponse> createModel(
            @CookieValue("SESSION") UUID sessionToken,
            @ModelAttribute @Valid ModelRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(modelService.createModel(sessionToken, request));
    }

    @PostMapping("/find-by-id")
    public ResponseEntity<ModelResponse> findById(
            @CookieValue("SESSION") UUID sessionToken,
            @RequestBody ModelRequest request
    ) {
        return ResponseEntity.ok(
                modelService.getAccountModel(sessionToken, request)
        );
    }

    @GetMapping("/find-by-account")
    public ResponseEntity<ModelResponse> findByAccountId(
            @CookieValue("SESSION") UUID sessionToken
    ) {
        return ResponseEntity.ok(
                modelService.getAccountModels(sessionToken)
        );
    }

    @DeleteMapping("/{modelId}")
    public ResponseEntity<Void> deleteById(
            @CookieValue("SESSION") UUID sessionToken,
            @PathVariable UUID modelId) {

        int rowsAffected = modelService.deleteModel(sessionToken, modelId);

        return rowsAffected > 0
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}