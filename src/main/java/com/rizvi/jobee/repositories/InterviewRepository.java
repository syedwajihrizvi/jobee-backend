package com.rizvi.jobee.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.rizvi.jobee.entities.Interview;;

public interface InterviewRepository extends JpaRepository<Interview, Long> {
    // Additional query methods can be defined here if needed
}
