package com.cloudfox.api.dto.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ModelResponse {
    private String name;
    private String fileName;
}