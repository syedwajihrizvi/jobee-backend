package com.rizvi.jobee.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.rizvi.jobee.entities.InterviewPreparation;

public interface InterviewPreparationRepository extends JpaRepository<InterviewPreparation, Long> {
    @Query("select ip from InterviewPreparation ip where ip.interview.id = :interviewId")
    InterviewPreparation findByInterviewId(Long interviewId);
}
