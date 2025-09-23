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
    @JsonPropertyDescription("Institution name")
    public String institution;
    @JsonPropertyDescription("Degree obtained")
    public String degree;
    @JsonPropertyDescription("Start year of education")
    public String fromYear;
    @JsonPropertyDescription("End year of education")
    public String toYear;

    public AIEducation(Education education) {
        this.institution = education.getInstitution();
        this.degree = education.getDegree();
        this.fromYear = education.getFromYear();
        this.toYear = education.getToYear();
    }

    public String toJsonString() {
        return """
                {"institutionName": "%s", "degree": "%s", "fromYear": "%s", "toYear": "%s"}
                """.formatted(institution, degree, fromYear, toYear);
    }
}
