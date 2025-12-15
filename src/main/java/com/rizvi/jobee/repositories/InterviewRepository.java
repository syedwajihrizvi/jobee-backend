package com.rizvi.jobee.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.rizvi.jobee.entities.Interview;

public interface InterviewRepository extends JpaRepository<Interview, Long> {

    Page<Interview> findAll(Specification<Interview> specification, Pageable pageable);

    @EntityGraph(attributePaths = { "interviewers.profile", "rejection", "candidate", "candidate.account", "createdBy",
            "job.company",
            "application", "interviewers", "rescheduleRequest" })
    @Query("select i from Interview i where i.id = :id")
    Optional<Interview> findById(Long id);

    // Additional query methods can be defined here if needed
    @Query("select i from Interview i where i.candidate.id = :candidateId")
    List<Interview> findByCandidateId(Long candidateId, Sort sort);

    @Query("select i from Interview i where i.job.id = :jobId and i.candidate.id = :candidateId")
    Interview findByJobIdAndCandidateId(Long jobId, Long candidateId);

    @EntityGraph(attributePaths = { "candidate" })
    @Query("select i from Interview i where i.job.id = :jobId")
    List<Interview> findByJobId(Long jobId, Sort sort);

    @Query(value = "SELECT * FROM INTERVIEWS WHERE JOB_ID = :jobId ORDER BY created_at LIMIT :limit", nativeQuery = true)
    List<Interview> findByJobIdWithLimit(Long jobId, Number limit, Sort sort);

    @EntityGraph(attributePaths = {
            "job", "candidate", "job.tags", "job.company",
            "interviewTips", "candidate.company", "candidate.skills", "candidate.skills.skill", "candidate.experiences",
            "candidate.projects", "candidate.education" })
    @Query("select i from Interview i where i.id = :interviewId")
    Interview findInterviewForPreparation(Long interviewId);

    @EntityGraph(attributePaths = { "job", "candidate", "createdBy", "createdBy.company", "interviewers.profile" })
    @Query("select i from Interview i where i.job.company.id = :companyId")
    List<Interview> findByCompanyId(Long companyId, Sort sort);

    @EntityGraph(attributePaths = { "job", "job.businessAccount" })
    @Query("select i from Interview i where i.job.businessAccount.id = :businessAccountId")
    List<Interview> findByCreatedAccountId(Long businessAccountId, Sort sort);

    @EntityGraph(attributePaths = { "job" })
    List<Interview> findByInterviewersId(Long interviewerId, Sort sort);

    @EntityGraph(attributePaths = { "application", "candidate" })
    @Query("select i from Interview i where i.id = :interviewId")
    Optional<Interview> findByInterviewWithApplication(Long interviewId);

}
