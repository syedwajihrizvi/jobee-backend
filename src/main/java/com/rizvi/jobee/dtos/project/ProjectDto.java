package com.rizvi.jobee.dtos.project;

import lombok.Data;

@Data
public class ProjectDto {
    private Long id;
    private String name;
    private String description;
    private String link;
    private String yearCompleted;
}
