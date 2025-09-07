package com.rizvi.jobee.dtos.job;

import com.rizvi.jobee.enums.ApplicationStatus;

import lombok.Data;

@Data
public class JobApplicationStatusDto {
    private JobSummaryDto job;
    private ApplicationStatus status;
}
