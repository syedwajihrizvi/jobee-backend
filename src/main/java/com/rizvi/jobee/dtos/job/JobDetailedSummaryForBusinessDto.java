package com.rizvi.jobee.dtos.job;

import java.time.LocalDate;
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
    private Integer applicants;
    private List<TagDto> tags;
    private LocalDate createdAt;
    private Integer totalShortListedCandidates;
}
