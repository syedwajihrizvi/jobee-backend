package com.rizvi.jobee.dtos.job;

import java.util.List;

import com.rizvi.jobee.enums.ApplicationStatus;

import lombok.Data;

@Data
public class JobApplicationStatusDto {
    private JobSummaryDto job;
    private ApplicationStatus status;
    private Long applicationId;
    private String appliedAt;
    private List<Long> interviewIds;
}
