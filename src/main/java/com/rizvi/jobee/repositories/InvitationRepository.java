package com.rizvi.jobee.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rizvi.jobee.entities.Invitation;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    Invitation findByCompanyCode(String companyCode);
}
