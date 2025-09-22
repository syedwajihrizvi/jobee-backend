package com.rizvi.jobee.entities;

import com.rizvi.jobee.enums.PreparationStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "interview_preparations")
public class InterviewPreparation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "created_at", nullable = true, insertable = false, updatable = false)
    private java.time.LocalDateTime createdAt;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PreparationStatus status;

    @OneToOne(optional = false)
    private Interview interview;
}
