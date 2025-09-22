package com.rizvi.jobee.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.rizvi.jobee.dtos.company.CompanyDto;
import com.rizvi.jobee.dtos.company.CreateCompanyDto;
import com.rizvi.jobee.entities.Company;
import com.rizvi.jobee.mappers.CompanyMapper;
import com.rizvi.jobee.repositories.CompanyRepository;

import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/companies")
public class CompanyController {
    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;

    @GetMapping()
    @Operation(summary = "Get all companies")
    public ResponseEntity<List<CompanyDto>> getAllCompanies() {
        var companies = companyRepository.findAll().stream().map(companyMapper::toCompanyDto).toList();
        return ResponseEntity.ok(companies);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a company by ID")
    public ResponseEntity<CompanyDto> getCompany(@PathVariable Long id) {
        var company = companyRepository.findById(id)
                .map(companyMapper::toCompanyDto);
        return company.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
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

    // Endpoint for updating company fields
}
