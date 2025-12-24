package com.rizvi.jobee.helpers.AISchemas;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class PostedJobInformationRequest {
    @JsonProperty("Job Details")
    private AIJob job;
    @JsonProperty("Company Details")
    private AICompany company;
    private List<String> existingTags;

    public String toJsonString() {
        return """
                {"Job": %s, "Company": %s, "Existing Tags": %s}
                """.formatted(job.toJsonString(), company.toJsonString(), existingTags.toString());
    }
}
