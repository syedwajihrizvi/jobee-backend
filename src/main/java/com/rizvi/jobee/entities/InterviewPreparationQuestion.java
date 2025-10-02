package com.rizvi.jobee.entities;

import com.rizvi.jobee.helpers.AISchemas.AnswerInterviewQuestionResponse;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "interview_preparation_questions")
public class InterviewPreparationQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "question", nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(name = "answer", nullable = true, columnDefinition = "TEXT")
    private String answer;

    @Column(name = "ai_answer", nullable = true, columnDefinition = "TEXT")
    private String aiAnswer;

    @Column(name = "user_answer_score", nullable = true)
    private Short userAnswerScore;

    @Column(name = "question_audio_url", nullable = true)
    private String questionAudioUrl;

    @Column(name = "answer_audio_url", nullable = true)
    private String answerAudioUrl;

    @Column(name = "ai_answer_audio_url", nullable = true)
    private String aiAnswerAudioUrl;

    @Column(name = "reason_for_score", nullable = true, columnDefinition = "TEXT")
    private String reasonForScore;

    @ManyToOne(optional = false, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "interview_preparation_id ", nullable = false)
    private InterviewPreparation interviewPreparation;

    public void addAIInterviewAnswer(AnswerInterviewQuestionResponse response, String audioFile) {
        this.setAiAnswerAudioUrl(audioFile);
        this.setUserAnswerScore(response.getScoreOfProvidedAnswer());
        this.setReasonForScore(response.getReasonForScore());
        this.setAiAnswer(response.getAnswer());
    }

    public void updateViaAiFeedback(AnswerInterviewQuestionResponse response) {
        this.setUserAnswerScore(response.getScoreOfProvidedAnswer());
        this.setReasonForScore(response.getReasonForScore());
    }
}
