package com.rizvi.jobee.helpers.AISchemas;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.rizvi.jobee.entities.Experience;

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
    @JsonPropertyDescription("Experience ID")
    public String id;
    @JsonPropertyDescription("Company name")
    public String company;
    @JsonPropertyDescription("Job title")
    public String title;
    @JsonPropertyDescription("Job city")
    public String city;
    @JsonPropertyDescription("Job country")
    public String country;
    @JsonPropertyDescription("Job state or province such as ON, CA, TX, NY etc.")
    public String state;
    @JsonPropertyDescription("Job description")
    public String description;
    @JsonPropertyDescription("Job start year")
    public String fromYear;
    @JsonPropertyDescription("Job end year")
    public String toYear;

    public AIExperience(Experience experience) {
        this.id = experience.getId().toString();
        this.company = experience.getCompany();
        this.city = experience.getCity();
        this.country = experience.getCountry();
        this.title = experience.getTitle();
        this.description = experience.getDescription();
        this.fromYear = experience.getFrom();
        this.toYear = experience.getTo();
    }

    public String toJsonString() {
        return """
                {"id": "%s", "company": "%s", "city": "%s", "country": "%s", "state": "%s", "position": "%s", "description": "%s", "fromYear": "%s", "toYear": "%s"}
                """
                .formatted(id, company, city, country, state, title, description, fromYear, toYear);
    }
}
