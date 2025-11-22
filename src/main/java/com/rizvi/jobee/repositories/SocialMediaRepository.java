package com.rizvi.jobee.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.rizvi.jobee.entities.Social;
import com.rizvi.jobee.enums.SocialType;

public interface SocialMediaRepository extends JpaRepository<Social, Long> {

    @Query("select s from Social s where s.userProfile.id = :userId")
    List<Social> findByUserProfileId(Long userId);

    @Query("select s from Social s where s.id = :id and s.userProfile.id = :userId")
    Social findByIdAndUserProfileId(Long id, Long userId);

    @Query("select s from Social s where s.type = :type and s.userProfile.id = :userId")
    Optional<Social> findByTypeAndUserProfileId(SocialType type, Long userId);
}
