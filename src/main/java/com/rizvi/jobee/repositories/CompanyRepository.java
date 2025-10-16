package com.rizvi.jobee.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.rizvi.jobee.entities.Company;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    // Additional query methods can be defined here if needed
    Company findByName(String name);

    @Query("select c from Company c where lower(c.name) like lower(concat('%', :name, '%'))")
    List<Company> findByNameContainingIgnoreCase(String name);
}
