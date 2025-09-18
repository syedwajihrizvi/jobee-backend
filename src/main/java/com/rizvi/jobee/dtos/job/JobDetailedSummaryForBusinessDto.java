package com.rizvi.jobee.dtos.job;

import java.time.LocalDateTime;
import java.util.List;

import com.rizvi.jobee.dtos.tag.TagDto;

import lombok.Data;

@Data
public class JobDetailedSummaryForBusinessDto {
    private Long id;
    private String title;
    private String description;
    private String employmentType;
    private String location;
    private Integer minSalary;
    private Integer maxSalary;
    private String setting;
    private Integer applicants;
    private List<TagDto> tags;
    private LocalDateTime createdAt;
    private Integer totalShortListedCandidates;
    private LocalDateTime appDeadline;
}
