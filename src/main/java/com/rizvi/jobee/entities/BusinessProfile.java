package com.rizvi.jobee.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "business_profiles")
public class BusinessProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "profile_image_url", nullable = true)
    private String profileImageUrl;

    @Column(name = "verified", nullable = false)
    @Builder.Default
    private Boolean verified = false;

    @Column(name = "city", nullable = true)
    private String city;

    @Column(name = "state", nullable = true)
    private String state;

    @Column(name = "country", nullable = true)
    private String country;

    @Column(name = "summary", nullable = true, columnDefinition = "TEXT")
    private String summary;

    @OneToOne(optional = false)
    @JoinColumn(name = "business_account_id", nullable = false, unique = true)
    private BusinessAccount businessAccount;

    public String getLocation() {
        if (city == null && state == null && country == null) {
            return null;
        }
        
        StringBuilder location = new StringBuilder();
        if (city != null && !city.trim().isEmpty()) {
            location.append(city);
        }
        if (state != null && !state.trim().isEmpty()) {
            if (location.length() > 0) location.append(", ");
            location.append(state);
        }
        if (country != null && !country.trim().isEmpty()) {
            if (location.length() > 0) location.append(", ");
            location.append(country);
        }
        
        return location.toString();
    }
}
