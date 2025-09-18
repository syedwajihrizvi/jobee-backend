package com.rizvi.jobee.helpers.AISchemas;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class AIExperience {
    @JsonPropertyDescription("Company name")
    public String company;
    @JsonPropertyDescription("Job title")
    public String title;
    @JsonPropertyDescription("Job description")
    public String description;
    @JsonPropertyDescription("Job start year")
    public String fromYear;
    @JsonPropertyDescription("Job end year")
    public String toYear;
}
