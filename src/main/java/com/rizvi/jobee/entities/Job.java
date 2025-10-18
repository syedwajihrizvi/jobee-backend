package com.rizvi.jobee.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.rizvi.jobee.enums.ApplicationStatus;
import com.rizvi.jobee.enums.EmploymentType;
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
    private Integer views;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "created_at", nullable = true, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "location", nullable = true)
    private String location;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "employment_type", nullable = true)
    private EmploymentType employmentType;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "setting", nullable = false)
    private JobSetting setting;

    @Column(name = "min_salary", nullable = true)
    private Integer minSalary;

    @Column(name = "max_salary", nullable = true)
    private Integer maxSalary;

    @Column(name = "experience", nullable = false)
    private Integer experience;

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
        return applications.stream().filter(application -> application.getShortListed() == true).toList();
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
        for (Job job : jobs) {
            applications.addAll(job.getApplications());
        }
        return applications;
    }

}
