package com.rizvi.jobee.helpers.AISchemas;

import lombok.Data;

@Data
public class AIJobDescriptionResponse {
    private String aiGeneratedJobDescription;

    public AIJobDescriptionResponse(String aiGeneratedJobDescription) {
        this.aiGeneratedJobDescription = aiGeneratedJobDescription;
    }
}
