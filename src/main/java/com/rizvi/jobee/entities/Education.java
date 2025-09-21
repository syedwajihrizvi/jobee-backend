package com.rizvi.jobee.entities;

import java.time.LocalDateTime;

import com.rizvi.jobee.enums.EducationLevel;
import com.rizvi.jobee.helpers.AISchemas.AIEducation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "educations")
public class Education {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "from_year", nullable = false)
    private String fromYear;

    @Column(name = "to_year", nullable = true)
    private String toYear;

    @Column(name = "degree", nullable = false)
    private String degree;

    @Column(name = "institution", nullable = true)
    private String institution;

    @Column(name = "description", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false)
    private EducationLevel level;

    @Column(name = "created_at", nullable = true, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_profile_id", nullable = false)
    private UserProfile userProfile;

    public boolean isNew(AIEducation education) {
        String degree = normalizeString(education.degree);
        String institution = normalizeString(education.institution);
        String fromYear = education.fromYear;
        String toYear = education.toYear;
        String eduDegree = normalizeString(degree);
        String eduInstitution = normalizeString(institution);
        String eduFromYear = fromYear;
        String eduToYear = toYear;
        var degreeMatch = !eduDegree.isEmpty() && !degree.isEmpty() && eduDegree.equals(degree);
        var institutionMatch = !eduInstitution.isEmpty() && !institution.isEmpty()
                && eduInstitution.equals(institution);
        var fromYearMatch = yearsFromDBMatch(fromYear, eduFromYear);
        var toYearMatch = yearsFromDBMatch(eduToYear, toYear);
        return (degreeMatch && institutionMatch && fromYearMatch && toYearMatch);
    }

    private String normalizeString(String input) {
        if (input == null)
            return "";
        return input.replace(" ", "").toLowerCase();
    }

    private boolean isEmptyOrNull(String str) {
        return str == null || str.trim().isEmpty();
    }

    private boolean yearsFromDBMatch(String year1, String year2) {
        if (year1 == null && year2 == null)
            return true;
        if (year1 != null && year2 != null && year1.equals(year2))
            return true;
        return isEmptyOrNull(year1) && isEmptyOrNull(year2);
    }
}
