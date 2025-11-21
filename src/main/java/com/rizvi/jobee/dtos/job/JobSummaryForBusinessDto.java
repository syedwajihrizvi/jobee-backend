package com.rizvi.jobee.dtos.job;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class JobSummaryForBusinessDto {
    private Long id;
    private String title;
    private String location;
    private String description;
    private String level;
    private Integer applicants;
    private Integer totalInterviews;
    private String department;
    private String city;
    private String country;
    private Integer pendingApplicationsSize;
    private Integer minSalary;
    private Integer maxSalary;
    private Integer views;
    private String businessName;
    private String employmentType;
    private String setting;
    private LocalDateTime createdAt;
    private LocalDateTime appDeadline;
}
