package com.rizvi.jobee.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.rizvi.jobee.entities.Interview;;

public interface InterviewRepository extends JpaRepository<Interview, Long> {
    // Additional query methods can be defined here if needed
    List<Interview> findByCandidateId(Long candidateId);

    @Query("select i from Interview i where i.job.id = :jobId and i.candidate.id = :candidateId")
    Interview findByJobIdAndCandidateId(Long jobId, Long candidateId);
}
