package com.rizvi.jobee.entities;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;

import com.rizvi.jobee.enums.ApplicationStatus;
import com.rizvi.jobee.enums.InterviewDecisionResult;
import com.rizvi.jobee.enums.InterviewStatus;

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
import jakarta.persistence.OneToOne;
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
    private Boolean shortlisted = false;

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

    @OneToOne(mappedBy = "application", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private InterviewRejection rejection;

    @OneToOne(mappedBy = "application", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private UnofficalJobOffer jobOffer;

    @OneToMany(mappedBy = "application", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Interview> interviews = new HashSet<>();

    public List<UserDocument> getUserDocuments() {
        List<UserDocument> documents = new java.util.ArrayList<>();
        userProfile.getDocuments().stream()
                .filter(doc -> doc.getIsViewableByEmployers())
                .forEach(doc -> documents.add(doc));
        return documents;
    }

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

    public void updateJobOfferStatus(Boolean status) {
        if (this.jobOffer != null) {
            this.jobOffer.setAccepted(status);
            this.jobOffer.setUserAction(true);
            if (status) {
                this.status = ApplicationStatus.OFFER_ACCEPTED;
                for (Interview interview : this.interviews) {
                    interview.setDecisionResult(InterviewDecisionResult.OFFER_ACCEPTED);
                }
            } else {
                this.status = ApplicationStatus.OFFER_REJECTED;
                for (Interview interview : this.interviews) {
                    interview.setDecisionResult(InterviewDecisionResult.OFFER_REJECTED);
                }
            }
        }
    }
}
