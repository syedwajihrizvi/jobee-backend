package com.rizvi.jobee.helpers.AISchemas;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Education {
    @JsonPropertyDescription("Institution name")
    public String institution;
    @JsonPropertyDescription("Degree obtained")
    public String degree;
    @JsonPropertyDescription("Start year of education")
    public String fromYear;
    @JsonPropertyDescription("End year of education")
    public String toYear;
}
