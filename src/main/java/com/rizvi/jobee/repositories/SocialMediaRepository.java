package com.rizvi.jobee.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.rizvi.jobee.entities.Social;

public interface SocialMediaRepository extends JpaRepository<Social, Long> {

    @Query("select s from Social s where s.userProfile.id = :userId")
    List<Social> findByUserProfileId(Long userId);

    @Query("select s from Social s where s.id = :id and s.userProfile.id = :userId")
    Social findByIdAndUserProfileId(Long id, Long userId);
}
