package com.rizvi.jobee.dtos;

import lombok.Data;

@Data
public class CreateJobDto {
    private String title;
    private String description;
    private Long businessAccountId;
    private String location;
    private String employmentType;
    private Integer minSalary;
    private Integer maxSalary;
}
