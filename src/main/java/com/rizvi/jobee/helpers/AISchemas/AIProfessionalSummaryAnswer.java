package com.rizvi.jobee.helpers.AISchemas;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import lombok.Data;

@Data
public class AIProfessionalSummaryAnswer {
    @JsonPropertyDescription("The generated professional summary for the user.")
    private String professionalSummary;
}
