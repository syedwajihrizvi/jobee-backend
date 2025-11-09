package com.rizvi.jobee.dtos.job;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.rizvi.jobee.dtos.tag.TagDto;
import com.rizvi.jobee.dtos.user.HiringTeamMemberResponseDto;

import lombok.Data;

@Data
public class JobDetailedSummaryForBusinessDto {
    private Long id;
    private String title;
    private String description;
    private String employmentType;
    private String location;
    private String city;
    private String country;
    private String streetAddress;
    private String postalCode;
    private String department;
    private Long views;
    private Integer minSalary;
    private Integer maxSalary;
    private String level;
    private String setting;
    private Integer applicants;
    private Integer interviews;
    private List<TagDto> tags;
    private List<HiringTeamMemberResponseDto> hiringTeam = new ArrayList<>();
    private LocalDateTime createdAt;
    private Integer totalShortListedCandidates;
    private LocalDateTime appDeadline;
}
