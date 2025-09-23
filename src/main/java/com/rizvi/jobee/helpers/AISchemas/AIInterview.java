package com.rizvi.jobee.helpers.AISchemas;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.rizvi.jobee.entities.Interview;
import com.rizvi.jobee.helpers.ListUtils;

import lombok.Data;

@Data
public class AIInterview {
    @JsonPropertyDescription("Interview Title")
    private String title;
    @JsonPropertyDescription("Interview Description")
    private String description;
    @JsonPropertyDescription("Interview Preparation Tips from the company")
    private List<String> preparationTips;
    @JsonPropertyDescription("Interview Type")
    private String interviewType;

    public AIInterview(Interview interview) {
        this.title = interview.getTitle();
        this.description = interview.getDescription();
        this.preparationTips = interview.getPreparationTipsAsList();
        this.interviewType = interview.getInterviewType().name();
    }

    public String toJsonString() {
        return """
                {"title": "%s", "description": "%s", "preparationTips": [%s], "typeOfInterview": "%s"}
                """
                .formatted(title, description, ListUtils.listToJsonArrayString(preparationTips), interviewType);

    }
}
