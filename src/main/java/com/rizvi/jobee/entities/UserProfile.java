package com.rizvi.jobee.entities;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.rizvi.jobee.enums.ApplicationStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user_profiles")
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "title", nullable = true)
    private String title;

    @Column(name = "age", nullable = false)
    private Integer age;

    @Column(name = "summary", nullable = true)
    private String summary;

    @Column(name = "profile_image_url", nullable = true)
    private String profileImageUrl;

    @Column(name = "video_intro_url", nullable = true)
    private String videoIntroUrl;

    @Column(name = "city", nullable = true)
    private String city;

    @Column(name = "country", nullable = true)
    private String country;

    @Column(name = "state", nullable = true)
    private String state;

    @Column(name = "province", nullable = true)
    private String province;

    @Column(name = "phone_number", nullable = true)
    private String phoneNumber;

    @Column(name = "company", nullable = true)
    private String company;

    @Column(name = "profile_views", nullable = false)
    private Integer profileViews;

    @OneToOne(optional = false)
    @JoinColumn(name = "account_id", nullable = false, unique = true)
    private UserAccount account;

    @OneToMany(mappedBy = "candidate", orphanRemoval = true, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Interview> interviews = new HashSet<>();

    @OneToMany(mappedBy = "user", orphanRemoval = true, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private Set<UserDocument> documents = new HashSet<>();

    @OneToOne
    @JoinColumn(name = "primary_resume_id", unique = true)
    private UserDocument primaryResume;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "user_favorite_jobs", joinColumns = @JoinColumn(name = "user_profile_id"), inverseJoinColumns = @JoinColumn(name = "job_id"))
    private Set<Job> favoriteJobs = new HashSet<>();

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "favorite_companies", joinColumns = @JoinColumn(name = "user_profile_id"), inverseJoinColumns = @JoinColumn(name = "company_id"))
    private Set<Company> favoriteCompanies = new HashSet<>();

    @OneToMany(mappedBy = "userProfile", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<UserSkill> skills = new HashSet<>();

    @OneToMany(mappedBy = "userProfile", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Education> education = new HashSet<>();

    @OneToMany(mappedBy = "userProfile", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Experience> experiences = new HashSet<>();

    @OneToMany(mappedBy = "userProfile", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Project> projects = new HashSet<>();

    @OneToMany(mappedBy = "userProfile", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Social> socials = new HashSet<>();

    @OneToMany(mappedBy = "userProfile", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Application> applications = new HashSet<>();

    public void setAccount(UserAccount account) {
        this.account = account;
    }

    public String getLocation() {
        if (city == null && state == null && country == null) {
            return null;
        }

        StringBuilder location = new StringBuilder();
        if (city != null && !city.trim().isEmpty()) {
            location.append(city);
        }
        if (state != null && !state.trim().isEmpty()) {
            if (location.length() > 0)
                location.append(", ");
            location.append(state);
        }
        if (country != null && !country.trim().isEmpty()) {
            if (location.length() > 0)
                location.append(", ");
            location.append(country);
        }

        return location.toString();
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public Integer getTotalApplications() {
        return this.applications.size();
    }

    public Integer getRejectedApplications() {
        return (int) this.applications.stream()
                .filter(app -> app.getStatus() == ApplicationStatus.REJECTED)
                .count();
    }

    public Integer getInConsiderationApplications() {
        return (int) this.applications.stream()
                .filter(app -> app.getStatus() != ApplicationStatus.REJECTED)
                .count();
    }

    public Integer geTotalApplicationsInInterview() {
        return (int) this.applications.stream()
                .filter(app -> app.getStatus() == ApplicationStatus.INTERVIEW_SCHEDULED)
                .count();
    }

    public Application getLastApplication() {
        return this.applications.stream()
                .sorted((a1, a2) -> a2.getCreatedAt().compareTo(a1.getCreatedAt()))
                .findFirst()
                .orElse(null);
    }

    public void toggleFavoriteJob(Job job) {
        if (job != null) {
            if (this.favoriteJobs.contains(job)) {
                this.favoriteJobs.remove(job);
            } else {
                this.favoriteJobs.add(job);
            }
        }
    }

    public void addDocument(UserDocument document) {
        if (document != null) {
            this.documents.add(document);
            document.setUser(this);
        }
    }

    public void addSkill(UserSkill userSkill) {
        if (userSkill != null) {
            this.skills.add(userSkill);
            userSkill.setUserProfile(this);
        }
    }

    public void addEducation(Education education) {
        if (education != null) {
            this.education.add(education);
            education.setUserProfile(this);
        }
    }

    public List<String> getSkillsAsStringList() {
        return this.skills.stream().map(s -> s.getSkill().getName()).toList();
    }

    public Boolean canGenerateAIProfessionalSummary() {
        return this.education != null || !this.education.isEmpty()
                || this.experiences != null || !this.experiences.isEmpty() || this.skills != null
                || !this.skills.isEmpty() || this.projects != null && !this.projects.isEmpty();
    }
}