package com.rizvi.jobee.dtos.project;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateProjectDto {
    private String name;
    private String description;
    private String link;
    private String yearCompleted;
}
