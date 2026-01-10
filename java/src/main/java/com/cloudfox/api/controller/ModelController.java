package com.cloudfox.api.controller;


import com.cloudfox.api.dto.request.ModelRequest;
import com.cloudfox.api.dto.response.ModelResponse;
import com.cloudfox.api.service.ModelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cloudfox-api/v1/model")
@RequiredArgsConstructor
public class ModelController {

    private final ModelService modelService;

    @PostMapping("/create")
    public ResponseEntity<ModelResponse> createModel(@RequestBody @Valid ModelRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(modelService.createModel(request));
    }
}