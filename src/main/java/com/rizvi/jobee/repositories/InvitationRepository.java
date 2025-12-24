package com.rizvi.jobee.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.rizvi.jobee.entities.Invitation;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    @Query("select i from Invitation i where lower(i.companyCode) = lower(:companyCode)")
    Invitation findByCompanyCode(String companyCode);
}
