package com.cloudfox.api.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ModelResponse {
    private String name;
    private String fileName;
    private List<ModelDTO> models;
    private ModelDTO model;
}