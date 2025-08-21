package com.rizvi.jobee.repositories;

import org.springframework.data.repository.CrudRepository;

import com.rizvi.jobee.entities.Education;

public interface EducationRepository extends CrudRepository<Education, Long> {
    // Custom query methods can be defined here if needed

}
