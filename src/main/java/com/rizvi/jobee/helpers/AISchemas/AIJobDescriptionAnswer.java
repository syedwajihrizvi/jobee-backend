package com.rizvi.jobee.helpers.AISchemas;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import lombok.Data;

@Data
public class AIJobDescriptionAnswer {
    @JsonPropertyDescription("The AI generated job description")
    private String answer;
}
