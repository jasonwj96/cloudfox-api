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

@RestController
@RequestMapping("/cloudfox-api/v1/model")
@RequiredArgsConstructor
public class ModelController {

    private final ModelService modelService;

    @PostMapping(
            value = "/create",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ModelResponse> createModel(@ModelAttribute @Valid ModelRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(modelService.createModel(request));
    }

    @PostMapping("/find-by-id")
    public ResponseEntity<ModelResponse> findById(@RequestBody ModelRequest request) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(modelService.getAccountModel(request));
    }

    @PostMapping("/find-by-account-id")
    public ResponseEntity<ModelResponse> findByAccountId(@RequestBody ModelRequest request) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(modelService.getAccountModels(request));
    }
}