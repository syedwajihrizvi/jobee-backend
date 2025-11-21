package com.rizvi.jobee.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.rizvi.jobee.enums.ApplicationStatus;
import com.rizvi.jobee.enums.EmploymentType;
import com.rizvi.jobee.enums.JobLevel;
import com.rizvi.jobee.enums.JobSetting;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
@Table(name = "jobs")
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "views", nullable = false)
    @Builder.Default
    private Integer views = 0;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "department", nullable = true)
    private String department;

    @Column(name = "created_at", nullable = true, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "content_updated_at", nullable = true, insertable = false, updatable = false)
    private LocalDateTime contentUpdatedAt;

    @Column(name = "location", nullable = true)
    private String location;

    @Column(name = "city", nullable = true)
    private String city;

    @Column(name = "state", nullable = true)
    private String state;

    @Column(name = "country", nullable = true)
    private String country;

    @Column(name = "street_address", nullable = true)
    private String streetAddress;

    @Column(name = "postal_code", nullable = true)
    private String postalCode;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "employment_type", nullable = true)
    private EmploymentType employmentType;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "level", nullable = true)
    private JobLevel level;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "setting", nullable = false)
    private JobSetting setting;

    @Column(name = "min_salary", nullable = true)
    private Integer minSalary;

    @Column(name = "max_salary", nullable = true)
    private Integer maxSalary;

    @Column(name = "app_deadline", nullable = false)
    private LocalDateTime appDeadline;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_account_id", nullable = false)
    private BusinessAccount businessAccount;

    @OneToMany(mappedBy = "job", orphanRemoval = true, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Application> applications = new HashSet<>();

    @OneToMany(mappedBy = "job", orphanRemoval = true, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Interview> interviews = new HashSet<>();

    @OneToMany(mappedBy = "job", orphanRemoval = true, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private Set<HiringTeam> hiringTeamMembers = new HashSet<>();

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "job_tags", joinColumns = @JoinColumn(name = "job_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags = new HashSet<>();

    public void setBusinessAccount(BusinessAccount businessAccount) {
        this.businessAccount = businessAccount;
        businessAccount.getJobs().add(this);
    }

    public List<Application> getPendingApplications() {
        return applications.stream().filter(application -> application.getStatus() == ApplicationStatus.PENDING)
                .toList();
    }

    public List<Application> getShortListedApplications() {
        return applications.stream().filter(application -> application.getShortlisted() == true).toList();
    }

    public void addTag(Tag tag) {
        this.tags.add(tag);
        tag.getJobs().add(this);
    }

    public List<String> getTagListInString() {
        return tags.stream().map(Tag::getName).toList();
    }

    public boolean hasUserApplied(Long userId) {
        return applications.stream().anyMatch((app) -> app.getUserProfile().getId().equals(userId));
    }

    public static List<Application> getApplicationsFromJobs(List<Job> jobs) {
        List<Application> applications = new ArrayList<>();
        if (jobs != null) {
            for (Job job : jobs) {
                if (job != null && job.getApplications() != null) {
                    applications.addAll(job.getApplications());
                }
            }
        }
        return applications;
    }

    private static Map<JobLevel, List<Float>> experienceMap = new HashMap<>() {
        {
            put(JobLevel.INTERN, List.of(0f, 1f));
            put(JobLevel.ENTRY, List.of(0f, 1f));
            put(JobLevel.JUNIOR_LEVEL, List.of(1f, 3f));
            put(JobLevel.MID_LEVEL, List.of(3f, 5f));
            put(JobLevel.SENIOR_LEVEL, List.of(5f, 10f));
            put(JobLevel.LEAD, List.of(10f, Float.MAX_VALUE));
        }
    };

    public Float getUserMatchWithExperience(Long experienceYears) {

        var requiredExperienceRange = experienceMap.get(this.level);
        Float minExp = requiredExperienceRange.get(0);
        Float maxExp = requiredExperienceRange.get(1);

        if (experienceYears <= maxExp && experienceYears >= minExp) {
            return 100f;
        }
        if (experienceYears < minExp) {
            Float diff = minExp - experienceYears;
            return Math.max(0, 100 - diff * 20);
        }
        if (experienceYears > maxExp) {
            Float diff = experienceYears - maxExp;
            return Math.max(0, 100 - diff * 10);
        }

        return 0f;
    }

    public Long getCompanyId() {
        return this.businessAccount.getCompany().getId();
    }

    public void addHiringTeamMember(HiringTeam member) {
        this.hiringTeamMembers.add(member);
        member.setJob(this);
    }

    public String getJobLocation() {
        StringBuilder locationBuilder = new StringBuilder();
        if (this.city != null && !this.city.isEmpty()) {
            locationBuilder.append(this.city);
        }
        if (this.state != null && !this.state.isEmpty()) {
            if (locationBuilder.length() > 0) {
                locationBuilder.append(", ");
            }
            locationBuilder.append(this.state);
        }
        if (this.country != null && !this.country.isEmpty()) {
            if (locationBuilder.length() > 0) {
                locationBuilder.append(", ");
            }
            locationBuilder.append(this.country);
        }
        return locationBuilder.toString();
    }
}
