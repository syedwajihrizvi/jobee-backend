package com.rizvi.jobee.entities;

import java.time.LocalDate;
import java.time.LocalTime;

import com.rizvi.jobee.enums.Timezone;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reschedule_interview_requests")
public class InterviewRescheduleRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "reason", nullable = false)
    private String reason;

    @Column(name = "requested_time", nullable = false)
    private LocalDate interviewDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "viewed", nullable = false)
    private boolean viewed;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "timezone", nullable = false)
    private Timezone timezone;

    @OneToOne
    @JoinColumn(name = "interview_id", unique = true)
    private Interview interview;
}
