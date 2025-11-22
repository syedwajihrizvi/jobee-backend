package com.rizvi.jobee.helpers.AISchemas;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.rizvi.jobee.entities.Project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class AIProject {
    @JsonPropertyDescription("Project ID")
    public String id;
    @JsonPropertyDescription("Project title")
    public String title;
    @JsonPropertyDescription("Project description")
    public String description;
    @JsonPropertyDescription("Year the project was completed")
    public String yearCompleted;
    @JsonPropertyDescription("Link to the project")
    public String link;

    public AIProject(Project project) {
        this.id = project.getId().toString();
        this.title = project.getName();
        this.description = project.getDescription();
        this.yearCompleted = project.getYearCompleted();
        this.link = project.getLink();
    }

    public String toJsonString() {
        return """
                {"id": "%s", "title": "%s", "description": "%s", "yearCompleted": "%s", "link": "%s"}
                """.formatted(id, title, description, yearCompleted, link);
    }
}
