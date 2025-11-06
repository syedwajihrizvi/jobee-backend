package com.rizvi.jobee.helpers.AISchemas;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import lombok.Data;

@Data
public class AIJobInsightAnswer {
    @JsonPropertyDescription("List of insights about the job posting provided by AI")
    private List<String> insights;
}
