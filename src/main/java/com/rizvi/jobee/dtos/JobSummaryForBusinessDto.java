package com.rizvi.jobee.dtos;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class JobSummaryForBusinessDto {
    private Long id;
    private String title;
    private String location;
    private Number applicants;
    private Number minSalary;
    private Number maxSalary;
    private String businessName;
    private String employmentType;
    private LocalDateTime createdAt;
}
