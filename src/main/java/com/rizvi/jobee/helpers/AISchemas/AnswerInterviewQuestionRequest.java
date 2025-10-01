package com.rizvi.jobee.helpers.AISchemas;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import lombok.Data;

@Data
public class AnswerInterviewQuestionRequest {
    @JsonPropertyDescription("Job Details")
    private AIJob job;
    @JsonPropertyDescription("Company Details")
    private AICompany company;
    @JsonPropertyDescription("Candidate Details")
    private AICandidate candidate;
    @JsonPropertyDescription("Interview Preparation Question")
    private InterviewPrepQuestion question;

    public String toJsonString() {
        return """
                {"Job": %s, "Company": %s, "InterviewQuestion": %s, "Candidate": %s}
                """.formatted(job.toJsonString(), company.toJsonString(), question.toJsonString(),
                candidate.toJsonString());
    }
}
