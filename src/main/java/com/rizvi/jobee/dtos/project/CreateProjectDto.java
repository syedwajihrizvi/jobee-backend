package com.rizvi.jobee.dtos.project;

import lombok.Data;

@Data
public class CreateProjectDto {
    private String name;
    private String description;
    private String link;
    private String yearCompleted;
}
