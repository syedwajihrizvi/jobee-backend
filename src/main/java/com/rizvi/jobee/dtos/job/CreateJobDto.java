package com.rizvi.jobee.dtos.job;

import java.time.LocalDateTime;
import java.util.List;

import com.rizvi.jobee.enums.EmploymentType;
import com.rizvi.jobee.enums.JobLevel;
import com.rizvi.jobee.enums.JobSetting;

import lombok.Data;

@Data
public class CreateJobDto {
    private String title;
    private String description;
    private Long businessAccountId;
    private String location;
    private EmploymentType employmentType;
    private JobSetting setting;
    private Integer minSalary;
    private Integer maxSalary;
    private List<String> tags;
    private JobLevel experience;
    private LocalDateTime appDeadline;
}
