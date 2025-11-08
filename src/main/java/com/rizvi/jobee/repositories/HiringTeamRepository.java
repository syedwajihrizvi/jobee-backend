package com.rizvi.jobee.repositories;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.rizvi.jobee.entities.HiringTeam;

public interface HiringTeamRepository extends JpaRepository<HiringTeam, Long> {

    @EntityGraph(attributePaths = { "job" })
    @Query("select ht from HiringTeam ht where ht.businessAccount.id = :businessAccountId")
    List<HiringTeam> findByBusinessAccountId(Long businessAccountId, Sort sort);

    @EntityGraph(attributePaths = { "job" })
    @Query("select ht from HiringTeam ht where ht.businessAccount.id = :businessAccountId and lower(ht.job.title) like lower(concat('%', :search, '%'))")
    List<HiringTeam> findByBusinessAccountIdAndJobTitle(Long businessAccountId, String jobTitle, Sort sort);
}
