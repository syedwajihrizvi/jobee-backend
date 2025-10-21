package com.rizvi.jobee.dtos.user;

import java.util.List;

import com.rizvi.jobee.dtos.education.EducationDto;
import com.rizvi.jobee.dtos.experience.ExperienceDto;
import com.rizvi.jobee.dtos.project.ProjectDto;
import com.rizvi.jobee.dtos.skill.UserSkillDto;

import lombok.Data;

@Data
public class UserProfileSummaryForBusinessDto {
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
    private UserDocumentDto primaryResume;
    private String profileImageUrl;
    private String videoIntroUrl;
    private List<UserSkillDto> skills;
    private List<EducationDto> education;
    private List<ExperienceDto> experiences;
    private List<ProjectDto> projects;
}
