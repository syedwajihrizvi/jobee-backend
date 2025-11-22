package com.rizvi.jobee.helpers.AISchemas;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.rizvi.jobee.entities.Education;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class AIEducation {
    @JsonPropertyDescription("Education ID")
    public String id;
    @JsonPropertyDescription("Institution name")
    public String institution;
    @JsonPropertyDescription("Degree obtained")
    public String degree;
    @JsonPropertyDescription("Start year of education")
    public String fromYear;
    @JsonPropertyDescription("End year of education")
    public String toYear;
    @JsonPropertyDescription("Education level such as BACHELORS, MASTERS, PHD etc.")
    public String level;

    public AIEducation(Education education) {
        this.id = education.getId().toString();
        this.institution = education.getInstitution();
        this.degree = education.getDegree();
        this.fromYear = education.getFromYear();
        this.toYear = education.getToYear();
        this.level = education.getLevel().toString();
    }

    public String toJsonString() {
        return """
                {"id": "%s", "institutionName": "%s", "degree": "%s", "fromYear": "%s", "toYear": "%s", "level": "%s"}
                """.formatted(id, institution, degree, fromYear, toYear, level);
    }
}
