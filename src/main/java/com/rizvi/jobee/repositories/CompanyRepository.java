package com.rizvi.jobee.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rizvi.jobee.entities.Company;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    // Additional query methods can be defined here if needed
    Company findByName(String name);
}
