package com.rizvi.jobee.helpers.AISchemas;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.rizvi.jobee.entities.Job;
import com.rizvi.jobee.helpers.ListUtils;

import lombok.Data;

@Data
public class AIJob {
    @JsonPropertyDescription("Job Title")
    private String title;
    @JsonPropertyDescription("Job Description")
    private String description;
    @JsonPropertyDescription("Required Skills")
    private List<String> skills;
    @JsonPropertyDescription("Minimum Salary")
    private Integer minSalary;
    @JsonPropertyDescription("Maximum Salary")
    private Integer maxSalary;
    @JsonPropertyDescription("Location")
    private String location;
    @JsonPropertyDescription("Experience")
    private Integer experience;

    public AIJob(Job job) {
        this.title = job.getTitle();
        this.description = job.getDescription();
        this.skills = job.getTagListInString();
        this.minSalary = job.getMinSalary();
        this.maxSalary = job.getMaxSalary();
        this.location = job.getLocation();
        this.experience = job.getExperience();
    }

    public String toJsonString() {
        return """
                {"title": "%s", "description": "%s", "skills": [%s], "minSalary": %d, "maxSalary": %d, "location": "%s", "experience": %d}
                """
                .formatted(title, description, ListUtils.listToJsonArrayString(skills), minSalary, maxSalary, location,
                        experience);
    }
}
