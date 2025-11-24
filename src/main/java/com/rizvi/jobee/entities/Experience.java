package com.rizvi.jobee.entities;

import com.rizvi.jobee.helpers.AISchemas.AIExperience;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "experiences")
public class Experience {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "from_year", nullable = false)
    private String from;

    @Column(name = "to_year", nullable = true)
    private String to;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "company", nullable = false)
    private String company;

    @Column(name = "description", nullable = true)
    private String description;

    @Column(name = "city", nullable = true)
    private String city;

    @Column(name = "country", nullable = true)
    private String country;

    @Column(name = "state", nullable = true)
    private String state;

    @ManyToOne
    @JoinColumn(name = "user_profile_id", nullable = false)
    private UserProfile userProfile;

    public boolean isNew(AIExperience experience) {
        String title = normalizeString(experience.getTitle());
        String company = normalizeString(experience.getCompany());
        String fromYear = experience.getFromYear();
        String toYear = experience.getToYear();
        String expTitle = normalizeString(title);
        String expCompany = normalizeString(company);
        String expFromYear = from;
        String expToYear = to;
        var titleMatch = !title.isEmpty() && !expTitle.isEmpty() && title.equals(expTitle);
        var companyMatch = !company.isEmpty() && !expCompany.isEmpty() && company.equals(expCompany);
        var fromYearMatch = yearsFromDBMatch(expFromYear, fromYear);
        var toYearMatch = yearsFromDBMatch(expToYear, toYear);
        return titleMatch && companyMatch && fromYearMatch && toYearMatch;
    }

    private String normalizeString(String input) {
        if (input == null)
            return null;
        return input.trim().toLowerCase();
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

    public String getLocation() {
        StringBuilder location = new StringBuilder();
        if (city == null && state == null && country == null) {
            return null;
        }
        if (city != null && !city.isEmpty()) {
            location.append(city);
        }
        if (state != null && !state.isEmpty()) {
            if (location.length() > 0) {
                location.append(", ");
            }
            location.append(state);
        }
        if (country != null && !country.isEmpty()) {
            if (location.length() > 0) {
                location.append(", ");
            }
            location.append(country);
        }
        return location.toString();
    }

}
