package com.rizvi.jobee.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.rizvi.jobee.entities.Education;

public interface EducationRepository extends CrudRepository<Education, Long> {
    @Query("select e from Education e where e.userProfile.id = :userProfileId")
    List<Education> findByUserProfileId(Long userProfileId);
}
