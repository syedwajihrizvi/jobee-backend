package com.rizvi.jobee.dtos.interview;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.rizvi.jobee.enums.PreparationStatus;

import lombok.Data;

@Data
public class InterviewDto {
    private Long id;
    private String title;
    private String description;
    private LocalDate interviewDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String status;
    private LocalDate createdAt;
    private Long jobId;
    private String companyName;
    private Long createdById;
    private String jobTitle;
    private Long candidateId;
    private Long applicationId;
    private String candidateProfileImageUrl;
    private String candidateName;
    private String candidateEmail;
    private String interviewType;
    private String decisionDate;
    private String decisionResult;
    private String location;
    private String meetingLink;
    private String phoneNumber;
    private String timezone;
    private List<String> preparationTipsFromInterviewer;
    private List<InterviewConductorDto> interviewers;
    private List<ConductorDto> otherInterviewers;
    private PreparationStatus preparationStatus;
}
