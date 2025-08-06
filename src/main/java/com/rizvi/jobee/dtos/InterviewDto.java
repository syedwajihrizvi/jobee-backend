package com.rizvi.jobee.dtos;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class InterviewDto {
    private Long id;
    private LocalDateTime scheduledTime;
    private String description;
    private String status;
    private LocalDate createdAt;
    private Long jobId;
    private String jobTitle;
    private Long candidateId;
    private String candidateName;
    private String candidateEmail;
    private Long interviewerId;
    private String interviewerEmail;
}
