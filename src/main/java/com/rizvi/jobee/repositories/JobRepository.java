package com.rizvi.jobee.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rizvi.jobee.entities.Job;

public interface JobRepository extends JpaRepository<Job, Long> {
    // Additional query methods can be defined here if needed

}
