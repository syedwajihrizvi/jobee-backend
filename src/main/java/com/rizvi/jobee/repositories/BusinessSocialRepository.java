package com.rizvi.jobee.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.rizvi.jobee.entities.BusinessSocial;

public interface BusinessSocialRepository extends JpaRepository<BusinessSocial, Long> {
    @Query("select s from BusinessSocial s where s.businessProfile.id = :userId")
    List<BusinessSocial> findByUserProfileId(Long userId);

    @Query("select s from BusinessSocial s where s.id = :id and s.businessProfile.id = :userId")
    BusinessSocial findByIdAndUserProfileId(Long id, Long userId);
}
