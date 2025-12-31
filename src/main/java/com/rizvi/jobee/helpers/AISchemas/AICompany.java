package com.rizvi.jobee.helpers.AISchemas;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.rizvi.jobee.entities.Company;

import lombok.Data;

@Data
public class AICompany {
    @JsonPropertyDescription("Company name")
    private String name;
    @JsonPropertyDescription("Company description")
    private String description;
    @JsonPropertyDescription("Year the company was founded")
    private Integer foundedYear;
    @JsonPropertyDescription("Number of employees in the company")
    private Integer numEmployees;
    @JsonPropertyDescription("Industry the company operates in")
    private String industry;

    public AICompany(Company company) {
        this.name = company.getName();
        this.description = company.getDescription();
        this.foundedYear = company.getFoundedYear();
        this.numEmployees = company.getNumEmployees();
        this.industry = company.getIndustry();
    }

    public String toJsonString() {
        return """
                {"name": "%s", "description": "%s", "foundedYear": %d, "numEmployees": %d, "industry": "%s"}
                """
                .formatted(name, description, foundedYear, numEmployees, industry);
    }
}
