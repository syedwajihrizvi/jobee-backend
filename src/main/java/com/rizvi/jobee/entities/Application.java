package com.rizvi.jobee.entities;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.rizvi.jobee.enums.ApplicationStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

    public void setJob(Job job) {
        this.job = job;
        job.getApplications().add(this);
    }
}
