package com.rizvi.jobee.helpers.AISchemas;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import lombok.Data;

@Data
public class AIJobTagsResponse {
    @JsonPropertyDescription("An array of relevant tags for the job posting")
    private List<String> tags;
}
