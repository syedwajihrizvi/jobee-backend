package com.rizvi.jobee.entities;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.rizvi.jobee.enums.PreparationStatus;
import com.rizvi.jobee.helpers.AISchemas.InterviewPrepQuestion;
import com.rizvi.jobee.helpers.AISchemas.InterviewPrepResource;
import com.rizvi.jobee.helpers.AISchemas.PrepareForInterviewResponse;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "interview_preparations")
public class InterviewPreparation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "created_at", nullable = true, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "interviewPreparation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<InterviewPreparationStrength> strengths = new HashSet<>();

    @OneToMany(mappedBy = "interviewPreparation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<InterviewPreparationWeakness> weaknesses = new HashSet<>();

    @OneToMany(mappedBy = "interviewPreparation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<InterviewPreparationQuestion> questions = new HashSet<>();

    @OneToMany(mappedBy = "interviewPreparation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<InterviewPreparationResource> resources = new HashSet<>();

    @Column(name = "overall_advice", nullable = true)
    private String overallAdvice;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PreparationStatus status;

    @OneToOne(optional = false)
    @JoinColumn(name = "interview_id", nullable = false, unique = true)
    private Interview interview;

    public void updateViaAIResponse(PrepareForInterviewResponse response) {
        // Update the neccessary fields
        this.overallAdvice = response.getOverallAdvice();
        this.status = PreparationStatus.COMPLETED;
        var strengths = response.getStrengths();
        for (String strength : strengths) {
            addStrength(strength);
        }
        var weaknesses = response.getWeaknesses();
        for (String weakness : weaknesses) {
            addWeakness(weakness);
        }
        var questions = response.getInterviewQuestions();
        for (InterviewPrepQuestion question : questions) {
            addQuestion(question);
        }
        var resources = response.getInterviewResources();
        for (InterviewPrepResource resource : resources) {
            addResource(resource);
        }
    }

    private void addStrength(String strength) {
        var newStrength = InterviewPreparationStrength.builder()
                .strength(strength)
                .interviewPreparation(this)
                .build();
        this.strengths.add(newStrength);
    }

    private void addWeakness(String weakness) {
        var newWeakness = InterviewPreparationWeakness.builder()
                .weakness(weakness)
                .interviewPreparation(this)
                .build();
        this.weaknesses.add(newWeakness);
    }

    private void addQuestion(InterviewPrepQuestion question) {
        var newQuestion = InterviewPreparationQuestion.builder()
                .question(question.getQuestion())
                .answer(question.getAnswer())
                .interviewPreparation(this)
                .build();
        this.questions.add(newQuestion);
    }

    private void addResource(InterviewPrepResource resource) {
        var newResource = InterviewPreparationResource.builder()
                .type(resource.getType())
                .title(resource.getTitle())
                .link(resource.getLink())
                .description(resource.getDescription())
                .interviewPreparation(this)
                .build();
        this.resources.add(newResource);
    }
}
