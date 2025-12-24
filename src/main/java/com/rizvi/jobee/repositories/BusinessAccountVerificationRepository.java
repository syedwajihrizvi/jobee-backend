package com.rizvi.jobee.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rizvi.jobee.entities.BusinessAccountVerification;

public interface BusinessAccountVerificationRepository extends JpaRepository<BusinessAccountVerification, Long> {
    BusinessAccountVerification findByBusinessAccountId(Long businessAccountId);
}
