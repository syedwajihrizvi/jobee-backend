package com.rizvi.jobee.dtos.application;

import java.time.LocalDateTime;

import com.rizvi.jobee.dtos.job.JobSummaryDto;

import lombok.Data;

@Data
public class ApplicationDto {
    private Long id;
    private JobSummaryDto job;
    private String jobTitle;
    private Long userId;
    private String userEmail;
    private LocalDateTime appliedAt;
    private String status;
}
