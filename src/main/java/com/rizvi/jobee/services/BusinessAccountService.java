package com.rizvi.jobee.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.rizvi.jobee.dtos.job.PaginatedResponse;
import com.rizvi.jobee.dtos.user.CreateBusinessAccountDto;
import com.rizvi.jobee.entities.BusinessAccount;
import com.rizvi.jobee.entities.BusinessProfile;
import com.rizvi.jobee.entities.Company;
import com.rizvi.jobee.enums.BusinessType;
import com.rizvi.jobee.exceptions.AlreadyRegisteredException;
import com.rizvi.jobee.queries.CompanyMemberQuery;
import com.rizvi.jobee.repositories.BusinessAccountRepository;
import com.rizvi.jobee.repositories.CompanyRepository;
import com.rizvi.jobee.specifications.CompanyMemberSpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BusinessAccountService {
    private final BusinessAccountRepository businessAccountRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;

    public BusinessAccount createBusinessAccount(CreateBusinessAccountDto request) {
        var companyName = request.getCompanyName();
        var companySlug = CompanyService.generateSlug(companyName);
        var company = companyRepository.findBySlug(companySlug);
        if (company != null) {
            System.out.println("Company with name " + companyName + " already exists");
            throw new AlreadyRegisteredException("Company with name " + companyName
                    + " exists. Please contact admin or support if you need an invite.");
        }
        var password = passwordEncoder.encode(request.getPassword());
        var businessAccount = BusinessAccount.builder()
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .password(password)
                .accountType(BusinessType.ADMIN)
                .build();
        var businessProfile = BusinessProfile.builder().businessAccount(businessAccount).build();
        businessAccount.setProfile(businessProfile);
        // Create the company
        var newCompany = Company.builder().name(companyName).slug(companySlug).build();
        newCompany.addBusinessAccount(businessAccount);
        companyRepository.save(newCompany);
        return businessAccount;
    }

    public PaginatedResponse<BusinessAccount> getAllCompanyMembers(CompanyMemberQuery query, Integer pageNumber,
            Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<BusinessAccount> page = businessAccountRepository.findAll(CompanyMemberSpecification.withFilters(query),
                pageable);
        var companyMembers = page.getContent();
        var hasMore = pageNumber < page.getTotalPages() - 1;
        var totalElements = page.getTotalElements();
        return new PaginatedResponse<>(hasMore, companyMembers, totalElements);
    }
}
