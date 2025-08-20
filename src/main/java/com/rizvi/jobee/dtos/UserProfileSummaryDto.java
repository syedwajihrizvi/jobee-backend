package com.rizvi.jobee.dtos;

import java.util.List;

import lombok.Data;

@Data
public class UserProfileSummaryDto {
    private Long id;
    private String firstName;
    private String lastName;
    private Integer age;
    private String summary;
    private String title;
    private String location;
    private String company;
    private String email;
    private String phoneNumber;
    private String profileImageUrl;
    private List<UserDocumentDto> documents;
    private List<JobIdDto> favoriteJobs;
    private List<UserSkillDto> skills;
    private List<EducationDto> education;
    private UserAccountSummaryDto account;
    private Boolean profileComplete;
}
