package com.rizvi.jobee.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rizvi.jobee.entities.Job;

public interface JobRepository extends JpaRepository<Job, Long> {
    @EntityGraph(attributePaths = { "businessAccount" })
    @Query("""
            select j from Job j
            join fetch j.businessAccount ba
            join fetch ba.company c
            where lower(j.title) like lower(concat('%', :search, '%'))
            or lower(j.description) like lower(concat('%', :search, '%'))
            or lower(c.name) like lower(concat('%', :search, '%'))
            """)
    List<Job> findBySearch(@Param("search") String search);

    @EntityGraph(attributePaths = { "businessAccount", "businessAccount.company", "tags" })
    List<Job> findAll();
}
