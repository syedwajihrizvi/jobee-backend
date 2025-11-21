package com.rizvi.jobee.services;

import com.rizvi.jobee.dtos.company.UpdateCompanyDto;
import com.rizvi.jobee.entities.Company;
import com.rizvi.jobee.exceptions.AmazonS3Exception;
import com.rizvi.jobee.exceptions.CompanyNotFoundException;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.rizvi.jobee.repositories.CompanyRepository;
import com.rizvi.jobee.repositories.JobRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CompanyService {
    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;
    private final S3Service s3Service;
    private static final Integer TOP_COMPANIES_LIMIT = 5;

    public static String generateSlug(String companyName) {
        return companyName.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
    }

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
                    company.setLogo((String) result[2]);
                    Long jobCount = (Long) result[3];
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
        if (request.getWebsite() != null) {
            company.setWebsite(request.getWebsite());
        }
        return companyRepository.save(company);
    }

    public Company updateCompanyLogo(Long companyId, MultipartFile profileImage) throws AmazonS3Exception {
        Company company = companyRepository.findById(companyId).orElseThrow(() -> new CompanyNotFoundException());
        try {
            s3Service.uploadCompanyLogo(companyId, profileImage);
            company.setLogo(companyId.toString());
            return companyRepository.save(company);
        } catch (Exception e) {
            throw new AmazonS3Exception("Failed to upload profile image", e);
        }
    }
}