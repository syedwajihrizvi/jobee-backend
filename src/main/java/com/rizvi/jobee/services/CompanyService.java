package com.rizvi.jobee.services;

import com.rizvi.jobee.entities.Company;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.rizvi.jobee.repositories.JobRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CompanyService {
    private final JobRepository jobRepository;

    private static final Integer TOP_COMPANIES_LIMIT = 5;

    public List<Map<Company, Long>> fetchTopHiringCompanies() {
        var results = jobRepository.findTopHiringCompanies(TOP_COMPANIES_LIMIT);
        System.out.println(results);
        return results.stream()
                .map(result -> {
                    // Create a Company object from the query result
                    var company = new Company();
                    company.setId((Long) result[0]);
                    company.setName((String) result[1]);
                    Long jobCount = (Long) result[2];
                    return Map.of(company, jobCount);
                })
                .toList();
    }
}
