package com.rizvi.jobee.dtos.user;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.rizvi.jobee.helpers.AISchemas.Education;
import com.rizvi.jobee.helpers.AISchemas.Experience;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ResumeExtract {
    @JsonPropertyDescription("Phone number extracted from resume")
    public String phoneNumber;
    @JsonPropertyDescription("Country extracted from resume")
    public String country;
    @JsonPropertyDescription("City extracted from resume")
    public String city;
    @JsonPropertyDescription("Current company extracted from resume")
    public String currentCompany;
    @JsonPropertyDescription("Current position extracted from resume")
    public String currentPosition;
    @JsonPropertyDescription("Skills extracted from resume")
    public List<String> skills;
    @JsonPropertyDescription("Experience extracted from resume")
    public List<Experience> experience;
    @JsonPropertyDescription("Education extracted from resume")
    public List<Education> education;
}
