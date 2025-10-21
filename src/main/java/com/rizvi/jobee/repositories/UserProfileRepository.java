package com.rizvi.jobee.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rizvi.jobee.entities.UserProfile;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    @EntityGraph(attributePaths = { "account", "skills", "skills.skill", "documents", "favoriteJobs", "education" })
    @Query("select p from UserProfile p where p.account.id = :accountId")
    Optional<UserProfile> findByAccountId(@Param("accountId") Long accountId);

    @EntityGraph(attributePaths = { "account", "documents", "favoriteJobs", "skills", "skills.skill", "education" })
    Optional<UserProfile> findById(Long id);

    @Query("select p from UserProfile p where p.id = :id")
    Optional<UserProfile> findUserById(@Param("id") Long id);

    @EntityGraph(attributePaths = { "account", "skills", "skills.skill", "documents", "favoriteJobs", "education" })
    @Query("select p from UserProfile p")
    List<UserProfile> findAllUserProfilesWithDetails();

}
