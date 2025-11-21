package com.rizvi.jobee.helpers.AISchemas;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rizvi.jobee.entities.UserProfile;

import lombok.Data;

@Data
public class GenerateAIProfessionalSummaryRequest {
    @JsonProperty("User Detaild")
    private AICandidate user;
    @JsonProperty("Existing Summary")
    private String summary;

    public GenerateAIProfessionalSummaryRequest(UserProfile userProfile, String summary) {
        this.user = new AICandidate(userProfile);
        this.summary = summary;
    }

    public String toJsonString() {
        return """
                {"user": %s, "summary": "%s"}
                """.formatted(user.toJsonString(), summary != null ? summary.replace("\"", "\\\"") : "");
    }
}
