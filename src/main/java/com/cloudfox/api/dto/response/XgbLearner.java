package com.cloudfox.api.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record XgbLearner(
        @JsonProperty("feature_names") List<String> featureNames,
        @JsonProperty("feature_types") List<String> featureTypes
) {
}
