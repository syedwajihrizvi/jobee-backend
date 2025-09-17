package com.rizvi.jobee.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.rizvi.jobee.entities.Job;

public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {
    @EntityGraph(attributePaths = { "businessAccount", "businessAccount.company", "tags", "applications" })
    List<Job> findAll(Specification<Job> spec);

    @EntityGraph(attributePaths = { "businessAccount", "businessAccount.company", "tags" })
    @Query("select j from Job j where j.id in :jobIds")
    List<Job> findJobWithIdList(List<Long> jobIds);

    @EntityGraph(attributePaths = { "businessAccount", "businessAccount.company",
            "applications" })
    @Query("select j from Job j where j.businessAccount.company.id = :companyId")
    List<Job> findByCompanyId(Long companyId);

    @EntityGraph(attributePaths = { "applications", "tags" })
    @Query("select j from Job j where j.id = :jobId")
    Optional<Job> findDetailedJobById(Long jobId);

    @Query(nativeQuery = true, value = """
            SELECT DISTINCT j.* FROM jobs j
            JOIN job_tags jt ON j.id = jt.job_id
            JOIN tags t ON jt.tag_id = t.id
            WHERE LOWER(TRIM(t.name)) IN :skills
            """)
    List<Job> findJobsWithSkills(List<String> skills);
}
