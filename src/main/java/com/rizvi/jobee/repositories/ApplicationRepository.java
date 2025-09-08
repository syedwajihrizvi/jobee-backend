package com.rizvi.jobee.repositories;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.rizvi.jobee.entities.Application;
import com.rizvi.jobee.entities.UserProfile;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long>, JpaSpecificationExecutor<Application> {

    @EntityGraph(attributePaths = { "userProfile", "userProfile.account", "userProfile.skills",
            "userProfile.skills.skill", "userProfile.education", "userProfile.experiences", "resumeDocument",
            "coverLetterDocument" })
    Optional<Application> findById(Long id);

    @Query("select a from Application a where a.job.id = :jobId and a.userProfile.id = :userProfileId")
    Application findByJobIdAndUserProfileId(Long jobId, Long userProfileId);

    @EntityGraph(attributePaths = { "job", "job.businessAccount", "job.businessAccount.company", "job.tags" })
    List<Application> findByUserProfile(UserProfile userProfile);

    @EntityGraph(attributePaths = { "job", "job.businessAccount", "job.businessAccount.company" })
    @Query("select a from Application a where a.userProfile.id = :userProfileId")
    List<Application> findByUserProfileId(Long userProfileId);

    @EntityGraph(attributePaths = { "job", "userProfile", "userProfile.account" })
    List<Application> findAll(Specification<Application> spec);

}
