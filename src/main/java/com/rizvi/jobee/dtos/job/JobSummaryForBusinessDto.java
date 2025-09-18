package com.rizvi.jobee.dtos.job;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class JobSummaryForBusinessDto {
    private Long id;
    private String title;
    private String location;
    private String description;
    private Number applicants;
    private Integer minSalary;
    private Integer maxSalary;
    private String businessName;
    private String employmentType;
    private String setting;
    private LocalDateTime createdAt;
    private LocalDateTime appDeadline;
}
