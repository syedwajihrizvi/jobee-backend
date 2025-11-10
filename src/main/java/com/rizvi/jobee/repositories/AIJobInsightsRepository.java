package com.rizvi.jobee.repositories;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.rizvi.jobee.entities.AIJobInsight;

public interface AIJobInsightsRepository extends JpaRepository<AIJobInsight, Long> {
    @Query("select a from AIJobInsight a where a.job.id = :jobId")
    List<AIJobInsight> findByJobId(Long jobId, Sort sort);
}
