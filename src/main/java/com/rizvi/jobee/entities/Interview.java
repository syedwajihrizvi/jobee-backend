package com.rizvi.jobee.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.hibernate.annotations.Type;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rizvi.jobee.dtos.interview.ConductorDto;
import com.rizvi.jobee.dtos.interview.CreateInterviewDto;
import com.rizvi.jobee.enums.ApplicationStatus;
import com.rizvi.jobee.enums.InterviewDecisionResult;
import com.rizvi.jobee.enums.InterviewMeetingPlatform;
import com.rizvi.jobee.enums.InterviewStatus;
import com.rizvi.jobee.enums.InterviewType;
import com.rizvi.jobee.enums.PreparationStatus;
import com.rizvi.jobee.enums.Timezone;
import com.vladmihalcea.hibernate.type.json.JsonType;

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
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "interviews")
public class Interview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = true)
    private String description;

    @Column(name = "interview_date", nullable = false)
    private LocalDate interviewDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "timezone", nullable = false)
    private Timezone timezone;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "interview_type", nullable = false)
    private InterviewType interviewType;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "meeting_platform", nullable = true)
    private InterviewMeetingPlatform interviewMeetingPlatform;

    @Column(name = "street_address", nullable = true)
    private String streetAddress;

    @Column(name = "building_name", nullable = true)
    private String buildingName;

    @Column(name = "parking_info", nullable = true)
    private String parkingInfo;

    @Column(name = "contact_instructions", nullable = true)
    private String contactInstructionsOnArrival;

    @Column(name = "phone_number", nullable = true)
    private String phoneNumber;

    @Column(name = "cancellation_reason", nullable = true)
    private String cancellationReason;

    @OneToMany(mappedBy = "interview", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private Set<InterviewTip> interviewTips = new HashSet<>();

    @Enumerated(value = EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InterviewStatus status;

    @Column(name = "decision_date", nullable = true)
    private LocalDateTime decisionDate;

    @Builder.Default
    @Enumerated(value = EnumType.STRING)
    @Column(name = "decision_result", nullable = true)
    private InterviewDecisionResult decisionResult = InterviewDecisionResult.PENDING;

    @Column(name = "created_at", nullable = true, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Type(JsonType.class)
    @Column(name = "online_meeting_information", columnDefinition = "jsonb", nullable = true)
    private JsonNode onlineMeetingInformation;

    @OneToOne(mappedBy = "interview", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private InterviewRescheduleRequest rescheduleRequest;

    @OneToOne(mappedBy = "interview", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private InterviewPreparation preparation;

    @OneToOne(mappedBy = "interview", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private InterviewRejection rejection;

    @ManyToOne
    @JoinColumn(name = "created_by_user_id", nullable = true)
    private BusinessAccount createdBy;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "application_id", nullable = true)
    private Application application;

    @ManyToOne
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne()
    @JoinColumn(name = "candidate_id", nullable = false)
    private UserProfile candidate;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "interview_conductors", joinColumns = {
            @JoinColumn(name = "interview_id") }, inverseJoinColumns = { @JoinColumn(name = "interviewer_id") })
    private Set<BusinessAccount> interviewers = new HashSet<>();

    @Type(JsonType.class)
    @Column(name = "other_interviewers", columnDefinition = "jsonb", nullable = true)
    @Builder.Default
    private Set<ConductorDto> otherInterviewers = new HashSet<>();

    public boolean interviewersInclude(String email) {
        for (BusinessAccount interviewer : interviewers) {
            if (interviewer.getEmail().equalsIgnoreCase(email)) {
                return true;
            }
        }
        for (ConductorDto interviewer : otherInterviewers) {
            if (interviewer.getEmail().equalsIgnoreCase(email)) {
                return true;
            }
        }
        return false;
    }

    public void addOtherInterviewer(ConductorDto interviewer) {
        otherInterviewers.add(interviewer);
    }

    public void addInterviewer(BusinessAccount interviewer) {
        this.interviewers.add(interviewer);
        interviewer.getInterviews().add(this);
    }

    public PreparationStatus getPreparationStatus() {
        if (this.preparation == null) {
            return PreparationStatus.NOT_STARTED;
        }
        return this.preparation.getStatus();
    }

    public List<String> getPreparationTipsAsList() {
        List<String> tips = new ArrayList<>();
        for (InterviewTip tip : this.interviewTips) {
            tips.add(tip.getTip());
        }
        return tips;
    }

    // Custom setter to maintain bidirectional relationship
    public void setApplication(Application application) {
        if (this.application != null && !this.application.equals(application)) {
            this.application.getInterviews().remove(this);
        }

        // Set the new application
        this.application = application;

        // Add to new application's interviews collection
        if (application != null && !application.getInterviews().contains(this)) {
            application.getInterviews().add(this);
        }
    }

    public void updateInterviewApplicationStatus() {
        this.application.setStatus(ApplicationStatus.PENDING);
    }

    public String getCandidateEmail() {
        return this.candidate.getAccount().getEmail();
    }

    public Long getApplicationId() {
        return this.application != null ? this.application.getId() : null;
    }

    public Long getJobId() {
        return this.job != null ? this.job.getId() : null;
    }

    public Long getCandidateId() {
        return this.candidate != null ? this.candidate.getId() : null;
    }

    public Long getCompanyId() {
        return this.job != null && this.job.getBusinessAccount() != null
                ? this.job.getBusinessAccount().getCompany().getId()
                : null;
    }

    public void updateMeetingPlatform(
            String meetingPlatformStr, ObjectMapper objectMapper,
            CreateInterviewDto request) {
        var meetingPlatform = InterviewMeetingPlatform.valueOf(meetingPlatformStr.toUpperCase());
        this.setInterviewMeetingPlatform(meetingPlatform);
        if (meetingPlatform == InterviewMeetingPlatform.ZOOM && request.getZoomMeetingDetails() != null) {
            var onlineMeetingInformation = request.getZoomMeetingDetails();
            var jsonNode = onlineMeetingInformation.toJsonNode(objectMapper);
            this.setOnlineMeetingInformation(jsonNode);
        } else if (meetingPlatform == InterviewMeetingPlatform.GOOGLE_MEET
                && request.getGoogleMeetingDetails() != null) {
            var onlineMeetingInformation = request.getGoogleMeetingDetails();
            var jsonNode = onlineMeetingInformation.toJsonNode(objectMapper);
            this.setOnlineMeetingInformation(jsonNode);
        }
    }

    public void clearAllInterviewTips() {
        this.interviewTips.clear();
    }

    public void clearAllInterviewersAndOtherInterviewers() {
        // Clear interviewers' reference to this interview
        for (BusinessAccount interviewer : this.interviewers) {
            interviewer.getInterviews().remove(this);
        }
        this.interviewers.clear();
        this.otherInterviewers.clear();
    }

    public void removeRescheduleRequest() {
        this.rescheduleRequest.setInterview(null);
        this.rescheduleRequest = null;
    }
}
