package com.rizvi.jobee.entities;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;

import com.rizvi.jobee.enums.ApplicationStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Builder
@AllArgsConstructor
@Table(name = "applications", uniqueConstraints = {
        @UniqueConstraint(name = "unique_user_job_application", columnNames = { "user_profile_id", "job_id" })
})
public class Application {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "status")
    @Enumerated(value = EnumType.STRING)
    @Builder.Default
    private ApplicationStatus status = ApplicationStatus.PENDING;

    @Builder.Default
    @Column(name = "short_listed", nullable = true, insertable = false, updatable = true)
    private Boolean shortListed = false;

    @Column(name = "created_at", nullable = true, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_profile_id", nullable = false)
    private UserProfile userProfile;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_document_id", nullable = false)
    private UserDocument resumeDocument;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "cover_letter_document_id", nullable = true)
    private UserDocument coverLetterDocument;

    @OneToMany(mappedBy = "application", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Interview> interviews = new HashSet<>();

    public void setJob(Job job) {
        this.job = job;
        job.getApplications().add(this);
    }

    public List<Long> getInterviewIds() {
        return this.interviews.stream()
                .map(Interview::getId)
                .toList();
    }

    public void addInterview(Interview interview) {
        if (interview != null) {
            this.interviews.add(interview);
            if (!this.equals(interview.getApplication())) {
                interview.setApplication(this);
            }
        }
    }

    public void removeInterview(Interview interview) {
        if (interview != null) {
            this.interviews.remove(interview);
            if (this.equals(interview.getApplication())) {
                interview.setApplication(null);
            }
        }
    }
}
