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
    private String streetAddress;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private String department;
    private JobSetting setting;
    private Integer minSalary;
    private Integer maxSalary;
    private List<String> tags;
    private List<HiringTeamMemberDto> hiringTeam;
    private JobLevel experience;
    private LocalDateTime appDeadline;
}
