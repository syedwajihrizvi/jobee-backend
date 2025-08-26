package com.rizvi.jobee.repositories;

import org.springframework.data.repository.CrudRepository;

import com.rizvi.jobee.entities.Company;

public interface CompanyRepository extends CrudRepository<Company, Long> {
    // Additional query methods can be defined here if needed
    Company findByName(String name);
}
