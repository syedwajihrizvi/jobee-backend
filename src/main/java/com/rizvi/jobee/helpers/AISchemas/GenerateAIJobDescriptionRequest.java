package com.rizvi.jobee.helpers.AISchemas;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.rizvi.jobee.dtos.job.CreateJobDto;
import com.rizvi.jobee.entities.Company;

import lombok.Data;

@Data
public class GenerateAIJobDescriptionRequest {
    @JsonPropertyDescription("Job Details")
    private AIJobCreationForm jobCreationForm;
    @JsonPropertyDescription("Company Details")
    private AICompany company;

    public GenerateAIJobDescriptionRequest(CreateJobDto job, Company company) {
        this.jobCreationForm = new AIJobCreationForm(job);
        this.company = new AICompany(company);
    }

    public String toJsonString() {
        return """
                {"Job": %s, "Company": %s}
                """.formatted(jobCreationForm.toJsonString(), company.toJsonString());
    }
}
