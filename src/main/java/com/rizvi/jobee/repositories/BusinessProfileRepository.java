package com.rizvi.jobee.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.rizvi.jobee.entities.BusinessProfile;

public interface BusinessProfileRepository extends JpaRepository<BusinessProfile, Long> {
    @EntityGraph(attributePaths = { "businessAccount" })
    @Query("select bp from BusinessProfile bp where bp.businessAccount.email = :email")
    BusinessProfile findBusinessProfileByEmail(String email);
}
