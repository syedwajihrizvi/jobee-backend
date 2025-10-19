package com.rizvi.jobee.dtos;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.rizvi.jobee.enums.InterviewType;

import lombok.Data;

@Data
public class CreateInterviewDto {
    private String title;
    private String description;
    private Long jobId;
    private Long candidateId;
    private LocalDate interviewDate;
    private LocalTime startTime;
    private Integer duration; // in minutes
    private InterviewType interviewType;
    private String location;
    private String meetingLink;
    private List<ConductorDto> conductors;
}
