package com.rizvi.jobee.dtos;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class UserApplicationDto {
    private Long id;
    private LocalDateTime appliedAt;
    private String status;
    private Long jobId;
}
