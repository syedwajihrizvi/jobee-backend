package com.rizvi.jobee.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rizvi.jobee.dtos.user.BusinessProfileSummaryForInterviewDto;
import com.rizvi.jobee.mappers.BusinessMapper;
import com.rizvi.jobee.services.BusinessProfileService;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/business-profiles")
public class BusinessProfileController {

    private final BusinessProfileService businessProfileService;
    private final BusinessMapper businessMapper;

    @GetMapping()
    public ResponseEntity<BusinessProfileSummaryForInterviewDto> getBusinessProfileSummaryForInterview(
            @RequestParam String email) {
        System.out.println("Fetching business profile for email: " + email);
        var businessProfile = businessProfileService.getBusinessProfileByEmail(email);
        if (businessProfile == null) {
            return ResponseEntity.notFound().build();
        }
        var dto = businessMapper.toBusinessProfileSummaryForInterviewDto(businessProfile);
        return ResponseEntity.ok(dto);
    }
}
