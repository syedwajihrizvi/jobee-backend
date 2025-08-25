package com.rizvi.jobee.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import com.rizvi.jobee.entities.Application;
import com.rizvi.jobee.entities.UserProfile;

import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    // Additional query methods can be defined here if needed
    @EntityGraph(attributePaths = { "job", "job.businessAccount", "job.businessAccount.company" })
    List<Application> findByUserProfile(UserProfile userProfile);
}
