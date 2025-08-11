package com.rizvi.jobee.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.rizvi.jobee.entities.UserProfile;

public interface UserProfileRepository extends CrudRepository<UserProfile, Long> {
    @EntityGraph(attributePaths = { "account" })
    @Query("select p from UserProfile p where p.account.id = :accountId")
    Optional<UserProfile> findByAccountId(@Param("accountId") Long accountId);
}
