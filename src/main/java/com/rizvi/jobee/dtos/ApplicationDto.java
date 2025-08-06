package com.rizvi.jobee.dtos;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ApplicationDto {
    private Long id;
    private Long jobId;
    private String jobTitle;
    private String companyName;
    private Long userId;
    private String userEmail;
    private LocalDateTime appliedAt;
    private String status;
}
