package com.rizvi.jobee.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rizvi.jobee.dtos.user.BusinessProfileSummaryForInterviewDto;
import com.rizvi.jobee.dtos.user.UpdateBusinessProfileGeneralInfoDto;
import com.rizvi.jobee.mappers.BusinessMapper;
import com.rizvi.jobee.principals.CustomPrincipal;
import com.rizvi.jobee.services.BusinessProfileService;

import io.swagger.v3.oas.annotations.Operation;
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
        var businessProfile = businessProfileService.getBusinessProfileByEmail(email);
        if (businessProfile == null) {
            return ResponseEntity.notFound().build();
        }
        var dto = businessMapper.toBusinessProfileSummaryForInterviewDto(businessProfile);
        return ResponseEntity.ok(dto);
    }

    @PatchMapping("/update-general-info/me")
    @Operation(summary = "Update business general info")
    public ResponseEntity<BusinessProfileSummaryForInterviewDto> updateBusinessProfileGeneralInfo(
            @RequestBody UpdateBusinessProfileGeneralInfoDto request,
            @AuthenticationPrincipal CustomPrincipal principal) {
        var userId = principal.getId();
        var businessProfile = businessProfileService.updateBusinessProfileGeneralInfo(request, userId);
        var dto = businessMapper.toBusinessProfileSummaryForInterviewDto(businessProfile);
        return ResponseEntity.ok(dto);
    }
}
