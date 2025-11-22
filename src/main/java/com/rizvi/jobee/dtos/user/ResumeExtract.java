package com.rizvi.jobee.dtos.user;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.rizvi.jobee.helpers.AISchemas.AIEducation;
import com.rizvi.jobee.helpers.AISchemas.AIExperience;
import com.rizvi.jobee.helpers.AISchemas.AIProject;
import com.rizvi.jobee.helpers.AISchemas.AISocialMedia;

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
    public List<AIExperience> experiences;
    @JsonPropertyDescription("Education extracted from resume")
    public List<AIEducation> educations;
    @JsonPropertyDescription("Projects extracted from resume")
    public List<AIProject> projects;
    @JsonPropertyDescription("Social Media links extracted from resume")
    public List<AISocialMedia> socialMediaLinks;

}
