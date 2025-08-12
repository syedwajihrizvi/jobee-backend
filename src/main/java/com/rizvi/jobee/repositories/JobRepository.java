package com.rizvi.jobee.repositories;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.rizvi.jobee.entities.Job;

public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {
    @EntityGraph(attributePaths = { "businessAccount", "businessAccount.company", "tags" })
    List<Job> findAll(Specification<Job> spec);
}
