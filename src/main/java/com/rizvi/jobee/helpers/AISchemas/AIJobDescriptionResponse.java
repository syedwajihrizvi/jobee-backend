package com.rizvi.jobee.helpers.AISchemas;

import java.util.List;

import lombok.Data;

@Data
public class AIJobDescriptionResponse {
    private String enhancedJobDescription;
    private List<String> seoKeywords;

    public AIJobDescriptionResponse(String enhancedJobDescription, List<String> seoKeywords) {
        this.enhancedJobDescription = enhancedJobDescription;
        this.seoKeywords = seoKeywords;
    }
}
