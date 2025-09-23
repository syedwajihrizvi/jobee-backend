package com.rizvi.jobee.helpers.AISchemas;

import com.rizvi.jobee.entities.Project;

import lombok.Data;

@Data
public class AIProject {
    private String title;
    private String description;
    private String yearCompleted;

    public AIProject(Project project) {
        this.title = project.getName();
        this.description = project.getDescription();
        this.yearCompleted = project.getYearCompleted();
    }

    public String toJsonString() {
        return """
                {"title": "%s", "description": "%s", "yearCompleted": "%s"}
                """.formatted(title, description, yearCompleted);
    }
}
