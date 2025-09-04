package com.rizvi.jobee.dtos;

import lombok.Data;

@Data
public class InterviewSummaryDto {
    private Long id;
    private String title;
    private String interviewDate;
    private String startTime;
    private String endTime;
    private String description;
    private String jobTitle;
    private String companyName;
    private String interviewType;
    private String timezone;
}