package com.cloudfox.api.dto.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ModelParam {
    private String featureName;
    private String dataType;
}
