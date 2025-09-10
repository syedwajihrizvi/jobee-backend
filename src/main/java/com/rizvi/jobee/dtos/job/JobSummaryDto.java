package com.rizvi.jobee.dtos.job;

import java.time.LocalDateTime;
import java.util.List;

import com.rizvi.jobee.dtos.tag.TagDto;

import lombok.Data;

@Data
public class JobSummaryDto {
    private Long id;
    private String title;
    private String description;
    private Long businessAccountId;
    private String businessName;
    private String location;
    private String employmentType;
    private Integer minSalary;
    private Integer maxSalary;
    private Integer experience;
    private LocalDateTime appDeadline;
    private List<TagDto> tags;
}
