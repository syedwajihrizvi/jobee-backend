package com.rizvi.jobee.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.hibernate.annotations.Type;

import com.rizvi.jobee.dtos.interview.ConductorDto;
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

    @Column(name = "location", nullable = false)
    private String location;

    @Column(name = "meeting_link", nullable = false)
    private String meetingLink;

    @Column(name = "phone_number", nullable = true)
    private String phoneNumber;

    @OneToMany(mappedBy = "interview", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private Set<InterviewTips> interviewTips = new HashSet<>();

    @Enumerated(value = EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InterviewStatus status;

    @Column(name = "created_at", nullable = true, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToOne(mappedBy = "interview", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private InterviewPreparation preparation;

    @ManyToOne
    @JoinColumn(name = "created_by_user_id", nullable = true)
    private BusinessAccount createdBy;

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
    private List<ConductorDto> otherInterviewers = new ArrayList<>();

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
        for (InterviewTips tip : this.interviewTips) {
            tips.add(tip.getTip());
        }
        return tips;
    }
}
