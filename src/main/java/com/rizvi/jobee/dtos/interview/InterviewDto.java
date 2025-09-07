package com.rizvi.jobee.dtos.interview;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

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
    private String candidateName;
    private String candidateEmail;
    private String interviewType;
    private String location;
    private String meetingLink;
    private String phoneNumber;
    private String timezone;
    private List<InterviewConductorDto> interviewers;
    private List<ConductorDto> otherInterviewers;
}
