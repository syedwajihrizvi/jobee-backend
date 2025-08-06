package com.rizvi.jobee.dtos;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class CreateInterviewDto {
    private Long jobId;
    private Long candidateId;
    private LocalDateTime scheduledTime;
    private String description;
}
