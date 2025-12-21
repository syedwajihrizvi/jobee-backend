package com.rizvi.jobee.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "interview_preparation_feedback")
public class InterviewPreparationFeedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "review_rating", nullable = false)
    private Integer reviewRating;

    @Column(name = "review_text", nullable = true)
    private String reviewText;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_preperation_id", nullable = false)
    private InterviewPreparation interviewPreparation;
}
