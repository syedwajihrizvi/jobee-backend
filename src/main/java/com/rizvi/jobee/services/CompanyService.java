package com.rizvi.jobee.services;

import com.rizvi.jobee.dtos.company.UpdateCompanyDto;
import com.rizvi.jobee.entities.Company;
import com.rizvi.jobee.exceptions.CompanyNotFoundException;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.rizvi.jobee.repositories.CompanyRepository;
import com.rizvi.jobee.repositories.JobRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CompanyService {
    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;
    private static final Integer TOP_COMPANIES_LIMIT = 5;

    public Company findCompanyById(Long companyId) {
        return companyRepository.findById(companyId).orElseThrow(() -> new CompanyNotFoundException());
    }

    public List<Map<Company, Long>> fetchTopHiringCompanies() {
        var results = jobRepository.findTopHiringCompanies(TOP_COMPANIES_LIMIT);
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

    public Company updateCompany(Long companyId, UpdateCompanyDto request) {
        Company company = companyRepository.findById(companyId).orElseThrow(() -> new CompanyNotFoundException());
        if (request.getName() != null) {
            company.setName(request.getName());
        }
        if (request.getIndustry() != null) {
            company.setIndustry(request.getIndustry());
        }
        if (request.getFoundedYear() != null) {
            company.setFoundedYear(request.getFoundedYear());
        }
        if (request.getNumEmployees() != null) {
            company.setNumEmployees(request.getNumEmployees());
        }
        if (request.getHqCity() != null) {
            company.setHqCity(request.getHqCity());
        }
        if (request.getHqState() != null) {
            company.setHqState(request.getHqState());
        }
        if (request.getHqCountry() != null) {
            company.setHqCountry(request.getHqCountry());
        }
        if (request.getDescription() != null) {
            company.setDescription(request.getDescription());
        }
        return companyRepository.save(company);
    }
}
