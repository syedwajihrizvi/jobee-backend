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
    private Long companyId;
    private Long businessAccountId;
    private String businessName;
    private String companyLogoUrl;
    private String location;
    private Integer views;
    private String level;
    private String department;
    private String streetAddress;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private String employmentType;
    private String setting;
    private Integer minSalary;
    private Integer maxSalary;
    private Integer experience;
    private Integer applicationCount;
    private LocalDateTime createdAt;
    private LocalDateTime appDeadline;
    private List<TagDto> tags;
}
