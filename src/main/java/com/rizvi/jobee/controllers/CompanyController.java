package com.rizvi.jobee.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import com.rizvi.jobee.dtos.company.CompanyDto;
import com.rizvi.jobee.dtos.company.CreateCompanyDto;
import com.rizvi.jobee.dtos.company.TopHiringCompanyDto;
import com.rizvi.jobee.dtos.company.UpdateCompanyDto;
import com.rizvi.jobee.entities.Company;
import com.rizvi.jobee.exceptions.AmazonS3Exception;
import com.rizvi.jobee.mappers.CompanyMapper;
import com.rizvi.jobee.principals.CustomPrincipal;
import com.rizvi.jobee.repositories.CompanyRepository;
import com.rizvi.jobee.services.CompanyService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/companies")
public class CompanyController {
    private final CompanyRepository companyRepository;
    private final CompanyService companyService;
    private final CompanyMapper companyMapper;

    @GetMapping()
    @Operation(summary = "Get all companies")
    public ResponseEntity<List<CompanyDto>> getAllCompanies(
            @RequestParam(required = false) String search) {
        List<Company> companies;
        if (search == null || search.isBlank()) {
            companies = companyRepository.findAll();
        } else {
            companies = companyRepository.findByNameContainingIgnoreCase(search);
        }
        return ResponseEntity.ok(companies.stream().map(companyMapper::toCompanyDto).toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a company by ID")
    public ResponseEntity<CompanyDto> getCompany(@PathVariable Long id) {
        var company = companyRepository.findById(id)
                .map(companyMapper::toCompanyDto);
        return company.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/top-hiring-companies")
    @Operation(summary = "Get top hiring companies who have posted the most jobs")
    public ResponseEntity<List<TopHiringCompanyDto>> getTopHiringCompanies() {
        var raw_results = companyService.fetchTopHiringCompanies();
        System.out.println(raw_results);
        raw_results.stream().forEach(entry -> {
            System.out.println("Company: " + entry.keySet().iterator().next().getName() + ", Job Count: "
                    + entry.values().iterator().next());
        });
        List<TopHiringCompanyDto> topCompanies = raw_results.stream()
                .map(entry -> companyMapper.map(new Object[] {
                        entry.keySet().iterator().next().getId(),
                        entry.keySet().iterator().next().getName(),
                        entry.keySet().iterator().next().getLogo(),
                        entry.values().iterator().next(),
                }))
                .toList();
        return ResponseEntity.ok(topCompanies);
    }

    @PostMapping()
    public ResponseEntity<CompanyDto> createCompany(
            @RequestBody CreateCompanyDto request,
            UriComponentsBuilder uriComponentsBuilder) {
        var company = new Company();
        company.setName(request.getName());
        var savedCompany = companyRepository.save(company);
        var uri = uriComponentsBuilder.path("/api/companies/{id}")
                .buildAndExpand(savedCompany.getId()).toUri();
        return ResponseEntity.created(uri).body(companyMapper.toCompanyDto(savedCompany));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update a company by ID")
    public ResponseEntity<CompanyDto> updateCompany(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomPrincipal principal,
            @RequestBody UpdateCompanyDto request) {
        var company = companyService.updateCompany(id, request);
        if (company == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(companyMapper.toCompanyDto(company));
    }

    @PatchMapping("/{id}/logo")
    @Operation(summary = "Update a company's logo by ID")
    public ResponseEntity<CompanyDto> updateCompanyLogo(
            @RequestParam("profileImage") MultipartFile profileImage,
            @PathVariable Long id,
            @AuthenticationPrincipal CustomPrincipal principal) throws AmazonS3Exception {
        if (profileImage.isEmpty()) {
            throw new IllegalArgumentException("Profile image file is empty");
        }
        var savedCompany = companyService.updateCompanyLogo(id, profileImage);
        return ResponseEntity.ok(companyMapper.toCompanyDto(savedCompany));
    }

}
