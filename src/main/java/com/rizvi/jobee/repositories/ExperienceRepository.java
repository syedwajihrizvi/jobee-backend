package com.rizvi.jobee.repositories;

import com.rizvi.jobee.entities.Experience;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ExperienceRepository extends JpaRepository<Experience, Long> {
    @Query("select e from Experience e where e.userProfile.id = :userProfileId")
    List<Experience> findByUserProfileId(Long userProfileId);
}
