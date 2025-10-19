package com.rizvi.jobee.dtos.user;

import java.time.Instant;
import java.util.List;

import com.rizvi.jobee.dtos.education.EducationDto;
import com.rizvi.jobee.dtos.experience.ExperienceDto;
import com.rizvi.jobee.dtos.job.JobIdDto;
import com.rizvi.jobee.dtos.project.ProjectDto;
import com.rizvi.jobee.dtos.skill.UserSkillDto;

import lombok.Data;

@Data
public class UserProfileSummaryDto {
    private Long id;
    private String firstName;
    private String lastName;
    private Boolean canQuickApplyBatch;
    private Integer age;
    private Instant nextQuickApplyBatchTime;
    private String summary;
    private String title;
    private String city;
    private String country;
    private String location;
    private String state;
    private String province;
    private String company;
    private String email;
    private String phoneNumber;
    private String profileImageUrl;
    private String videoIntroUrl;
    private List<UserDocumentDto> documents;
    private List<JobIdDto> favoriteJobs;
    private List<UserSkillDto> skills;
    private List<EducationDto> education;
    private List<ExperienceDto> experiences;
    private List<UserApplicationDto> applications;
    private List<ProjectDto> projects;
    private UserAccountSummaryDto account;
    private Boolean profileComplete;
    private UserDocumentDto primaryResume;
}
