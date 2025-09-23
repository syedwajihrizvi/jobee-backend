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

    public AICompany(Company company) {
        this.name = company.getName();
        this.description = company.getDescription();
    }

    public String toJsonString() {
        return """
                {"name": "%s", "description": "%s"}
                """.formatted(name, description);
    }
}
