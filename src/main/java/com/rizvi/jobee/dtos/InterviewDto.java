package com.rizvi.jobee.dtos;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class InterviewDto {
    private Long id;
    private LocalDateTime scheduledTime;
    private String description;
    private Integer duration;
    private String status;
    private LocalDate createdAt;
    private Long jobId;
    private Long createdById;
    private String jobTitle;
    private Long candidateId;
    private String candidateName;
    private String candidateEmail;
    private String interviewType;
    private String location;
    private String meetingLink;
    private List<InterviewConductorDto> interviewers;
    private List<ConductorDto> otherInterviewers;
}
