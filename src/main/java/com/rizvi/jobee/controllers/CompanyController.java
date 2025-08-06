package com.rizvi.jobee.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.rizvi.jobee.dtos.CreateCompanyDto;
import com.rizvi.jobee.entities.Company;
import com.rizvi.jobee.repositories.CompanyRepository;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/companies")
public class CompanyController {
    private final CompanyRepository companyRepository;

    @PostMapping()
    public ResponseEntity<Company> createCompany(
            @RequestBody CreateCompanyDto request,
            UriComponentsBuilder uriComponentsBuilder) {
        var company = new Company();
        company.setName(request.getName());
        var savedCompany = companyRepository.save(company);
        var uri = uriComponentsBuilder.path("/api/companies/{id}")
                .buildAndExpand(savedCompany.getId()).toUri();
        return ResponseEntity.created(uri).body(savedCompany);
    }
}
