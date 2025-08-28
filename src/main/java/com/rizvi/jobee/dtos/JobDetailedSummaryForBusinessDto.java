package com.rizvi.jobee.dtos;

import java.time.LocalDate;
import java.util.List;

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
}
