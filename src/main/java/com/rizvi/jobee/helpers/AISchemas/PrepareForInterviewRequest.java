package com.rizvi.jobee.helpers.AISchemas;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class PrepareForInterviewRequest {
    @JsonProperty("Job Details")
    private AIJob job;
    @JsonProperty("Company Details")
    private AICompany company;
    @JsonProperty("Candidate Details")
    private AICandidate candidate;
    @JsonProperty("Interview Details")
    private AIInterview interview;

    public String toJsonString() {
        return """
                {"Job": %s, "Company": %s, "Candidate": %s, "Interview": %s}
                """.formatted(job.toJsonString(), company.toJsonString(), candidate.toJsonString(),
                interview.toJsonString());
    }
}
