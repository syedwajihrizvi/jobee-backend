package com.rizvi.jobee.helpers.AISchemas;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.rizvi.jobee.entities.Company;
import com.rizvi.jobee.entities.Job;

import lombok.Data;

@Data
public class GenerateAIInsightRequest {
    @JsonPropertyDescription("Job Details")
    private AIJob job;

    @JsonPropertyDescription("Company Details")
    private AICompany company;

    public GenerateAIInsightRequest(Job job, Company company) {
        this.job = new AIJob(job);
        this.company = new AICompany(company);
    }

    public String toJsonString() {
        return """
                {"Job": %s, "Company": %s}
                """.formatted(job.toJsonString(), company.toJsonString());
    }
}
