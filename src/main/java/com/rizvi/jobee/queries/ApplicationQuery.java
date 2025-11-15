package com.rizvi.jobee.queries;

import java.util.List;

import com.rizvi.jobee.enums.ApplicationStatus;

import lombok.Data;

@Data
public class ApplicationQuery {
    private String search;
    private List<String> locations;
    private Long userProfileId;
    private Boolean shortlisted;
    private Long jobId;
    private List<String> skills;
    private String educationLevel;
    private String experienceLevel;
    private Boolean hasCoverLetter;
    private Boolean hasVideoIntro;
    private Integer applicationDateRange;
    private ApplicationStatus applicationStatus;
}
