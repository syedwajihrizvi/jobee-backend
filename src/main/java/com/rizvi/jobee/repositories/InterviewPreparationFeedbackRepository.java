package com.rizvi.jobee.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.rizvi.jobee.entities.InterviewPreparationFeedback;

public interface InterviewPreparationFeedbackRepository extends JpaRepository<InterviewPreparationFeedback, Long> {

    @Query("select f from InterviewPreparationFeedback f where f.interviewPreparation.id = :interviewPreparationId")
    InterviewPreparationFeedback findByInterviewPreparationId(Long interviewPreparationId);
}
