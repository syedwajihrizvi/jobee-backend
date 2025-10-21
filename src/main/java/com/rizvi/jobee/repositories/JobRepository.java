package com.rizvi.jobee.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
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
        @Query("select j from Job j where j.businessAccount.company.id = :companyId order by j.createdAt desc")
        List<Job> findByCompanyId(Long companyId);

        @EntityGraph(attributePaths = { "applications", "tags" })
        @Query("select j from Job j where j.id = :jobId")
        Optional<Job> findDetailedJobById(Long jobId);

        @EntityGraph(attributePaths = { "tags", "businessAccount", "businessAccount.company" })
        @Query("select distinct j from Job j join j.tags t where lower(trim(t.slug)) in :skills")
        List<Job> findJobsWithSkills(List<String> skills);

        @Query("select j.businessAccount.company.id, j.businessAccount.company.name, count(j) as jobCount from Job j group by j.businessAccount.company.id, j.businessAccount.company.name order by jobCount desc limit :limit")
        List<Object[]> findTopHiringCompanies(Integer limit);

        @Query("select j from Job j where j.businessAccount.id = :accountId")
        List<Job> findByBusinessAccountId(Long accountId, Sort sort);

        @Query("select j from Job j where j.businessAccount.id = :accountId and lower(j.title) like lower(concat('%', :search, '%'))")
        List<Job> findByBusinessAccountIdAndTitle(Long accountId, String search);

        @Query("select j from Job j where j.businessAccount.company.id = :companyId order by j.createdAt desc")
        List<Job> findMostRecentJobsByCompanyId(Long companyId);

}
