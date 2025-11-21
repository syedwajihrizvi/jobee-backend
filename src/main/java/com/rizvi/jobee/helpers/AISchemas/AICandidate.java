package com.rizvi.jobee.helpers.AISchemas;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.rizvi.jobee.entities.UserProfile;
import com.rizvi.jobee.helpers.ListUtils;

import lombok.Data;

@Data
public class AICandidate {
        @JsonPropertyDescription("Candidate Name")
        private String name;
        @JsonPropertyDescription("Candidate Title")
        private String title;
        @JsonPropertyDescription("Candidate Location")
        private String location;
        @JsonPropertyDescription("Existing Professional Summary")
        private String existingSummary;
        @JsonPropertyDescription("Candidate Age")
        private Integer age;
        @JsonPropertyDescription("Current Company")
        private String currentCompany;
        @JsonPropertyDescription("Skills")
        private List<String> skills;
        @JsonPropertyDescription("Candidate Educations")
        private List<AIEducation> education;
        @JsonPropertyDescription("Candidate Experiences")
        private List<AIExperience> experience;
        @JsonPropertyDescription("Candidate Projects")
        private List<AIProject> projects;

        public AICandidate(UserProfile userProfile) {
                this.title = userProfile.getTitle();
                this.age = userProfile.getAge();
                var company = userProfile.getCompany();
                this.currentCompany = company != null ? company : "";
                this.location = userProfile.getLocation();
                this.existingSummary = userProfile.getSummary();
                this.skills = userProfile.getSkillsAsStringList();
                this.education = userProfile.getEducation().stream().map(AIEducation::new).toList();
                this.experience = userProfile.getExperiences().stream().map(AIExperience::new).toList();
                this.projects = userProfile.getProjects().stream().map(AIProject::new).toList();

        }

        public String toJsonString() {
                var educationString = education.stream().map(AIEducation::toJsonString).reduce((a, b) -> a + "," + b)
                                .orElse("");
                var experienceString = experience.stream().map(AIExperience::toJsonString).reduce((a, b) -> a + "," + b)
                                .orElse("");
                var projectString = projects.stream().map(AIProject::toJsonString).reduce((a, b) -> a + "," + b)
                                .orElse("");
                return """
                                {"title": "%s", "age": %d, "location": "%s", "existingSummary": "%s", "currentCompany": "%s", "skills": [%s],
                                "education": [%s], "experience": [%s], "projects": [%s]}
                                """
                                .formatted(title, age, location, existingSummary, currentCompany,
                                                ListUtils.listToJsonArrayString(skills),
                                                educationString, experienceString, projectString);
        }
}
