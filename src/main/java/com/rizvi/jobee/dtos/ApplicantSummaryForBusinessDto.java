package com.rizvi.jobee.dtos;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ApplicantSummaryForBusinessDto {
    private Long id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String profileImageUrl;
    private LocalDateTime appliedAt;
    private String profileSummary;
    private String title;
}
