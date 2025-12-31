package com.rizvi.jobee.helpers.AISchemas;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import lombok.Data;

@Data
public class AIJobEnhancementAnswer {
    @JsonPropertyDescription("The AI generated job description")
    private String enhancedJobDescription;

    @JsonPropertyDescription("An array of relevant tags for the job posting that can be used for SEO purposes")
    private List<String> seoKeywords;

}
