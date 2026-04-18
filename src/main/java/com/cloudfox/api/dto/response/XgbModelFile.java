package com.cloudfox.api.dto.response;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record XgbModelFile(XgbLearner learner) {
}