package com.rizvi.jobee.dtos.application;

import java.time.LocalDate;

import lombok.Data;

@Data
public class ApplicationSummaryDto {
    private Long id;
    private LocalDate appliedAt;
    private String status;
    private Long interviewId;
    private Long jobId;
}
