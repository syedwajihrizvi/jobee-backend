package com.rizvi.jobee.helpers.AISchemas;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.rizvi.jobee.dtos.job.CreateJobDto;
import com.rizvi.jobee.entities.Job;
import com.rizvi.jobee.helpers.ListUtils;

import lombok.Data;

@Data
public class AIJobCreationForm {
    @JsonPropertyDescription("Job Title")
    private String title;
    @JsonPropertyDescription("Initial Job Description")
    private String initialDescription;
    @JsonPropertyDescription("Department")
    private String department;
    @JsonPropertyDescription("Required Skills")
    private List<String> skills;
    @JsonPropertyDescription("Minimum Salary")
    private Integer minSalary;
    @JsonPropertyDescription("Maximum Salary")
    private Integer maxSalary;
    @JsonPropertyDescription("Experience")
    private String experience;
    @JsonPropertyDescription("City")
    private String city;
    @JsonPropertyDescription("Country")
    private String country;
    @JsonPropertyDescription("Setting")
    private String setting;
    @JsonPropertyDescription("Street Address")
    private String streetAddress;

    public AIJobCreationForm(CreateJobDto job) {
        this.title = job.getTitle();
        this.initialDescription = job.getDescription();
        this.department = job.getDepartment();
        this.skills = job.getTags();
        this.minSalary = job.getMinSalary();
        this.maxSalary = job.getMaxSalary();
        this.experience = job.getExperience().name();
        this.city = job.getCity();
        this.country = job.getCountry();
        this.setting = job.getSetting().name();
        this.streetAddress = job.getStreetAddress();
    }

    public String toJsonString() {
        return """
                {"title": "%s", "initialDescription": "%s", "department": "%s", "skills": [%s], "minSalary": %d, "maxSalary": %d, "experience": "%s", "city": "%s", "country": "%s", "setting": "%s", "streetAddress": "%s"}
                """
                .formatted(title, initialDescription, department, ListUtils.listToJsonArrayString(skills), minSalary,
                        maxSalary, experience, city, country, setting, streetAddress);
    }
}
