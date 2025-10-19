package com.rizvi.jobee.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rizvi.jobee.entities.QuickApplyTS;

public interface QuickApplyTSRepository extends JpaRepository<QuickApplyTS, Long> {
    QuickApplyTS findByUserProfileId(Long userProfileId);
}
