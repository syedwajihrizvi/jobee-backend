package com.rizvi.jobee.queries;

import java.util.List;
import lombok.Data;

@Data
public class ApplicationQuery {
    private List<String> locations;
    private Long jobId;
    private List<String> skills;
    private String educationLevel;
    private String experienceLevel;
    private Boolean hasCoverLetter;
    private Boolean hasVideoIntro;
    private Integer applicationDateRange;
}
