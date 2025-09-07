package com.rizvi.jobee.dtos.job;

import java.util.List;

import com.rizvi.jobee.enums.EmploymentType;

import lombok.Data;

@Data
public class CreateJobDto {
    private String title;
    private String description;
    private Long businessAccountId;
    private String location;
    private EmploymentType employmentType;
    private Integer minSalary;
    private Integer maxSalary;
    private List<String> tags;
    private Integer experience;
}
