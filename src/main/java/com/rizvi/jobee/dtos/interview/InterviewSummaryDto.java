package com.rizvi.jobee.dtos.interview;

import java.util.List;

import lombok.Data;

@Data
public class InterviewSummaryDto {
    private Long id;
    private String title;
    private String interviewDate;
    private String startTime;
    private String endTime;
    private String description;
    private String candidateName;
    private String candidateProfileImageUrl;
    private String jobId;
    private String jobTitle;
    private String companyName;
    private String interviewType;
    private String status;
    private String decisionDate;
    private String decisionResult;
    private String timezone;
    private List<InterviewConductorDto> interviewers;
    private List<ConductorDto> otherInterviewers;
}