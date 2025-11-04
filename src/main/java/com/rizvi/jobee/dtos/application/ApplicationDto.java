package com.rizvi.jobee.dtos.application;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ApplicationDto {
    private Long id;
    private Long jobId;
    private String jobTitle;
    private String companyName;
    private String companyLogoUrl;
    private Long userId;
    private String userEmail;
    private LocalDateTime appliedAt;
    private String status;
}
