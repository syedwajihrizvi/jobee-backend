package com.rizvi.jobee.dtos;

import java.util.List;

import lombok.Data;

@Data
public class JobSummaryDto {
    private Long id;
    private String title;
    private String description;
    private Integer applicationCount;
    private Integer views;
    private Long businessAccountId;
    private String companyName;
    private String companyLogoUrl;
    private String location;
    private String level;
    private String employmentType;
    private Integer minSalary;
    private Integer maxSalary;
    private Integer experience;
    private List<TagDto> tags;
}
