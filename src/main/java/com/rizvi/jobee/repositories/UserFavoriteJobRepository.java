package com.rizvi.jobee.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rizvi.jobee.entities.UserFavoriteJob;

public interface UserFavoriteJobRepository extends JpaRepository<UserFavoriteJob, Long> {

    @EntityGraph(attributePaths = { "job", "job.businessAccount", "job.businessAccount.company", "job.tags" })
    @Query("SELECT ufj FROM UserFavoriteJob ufj WHERE ufj.userProfile.id = :userProfileId")
    Page<UserFavoriteJob> findByUserProfileId(@Param("userProfileId") Long userProfileId, Pageable pageable);

}