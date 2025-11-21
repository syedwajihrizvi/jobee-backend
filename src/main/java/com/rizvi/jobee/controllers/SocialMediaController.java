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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.rizvi.jobee.dtos.socialMedia.CreateSocialMediaDto;
import com.rizvi.jobee.dtos.socialMedia.SocialMediaDto;
import com.rizvi.jobee.mappers.SocialMediaMapper;
import com.rizvi.jobee.principals.CustomPrincipal;
import com.rizvi.jobee.services.SocialMediaService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/profiles/socialMedia")
public class SocialMediaController {
    private final SocialMediaService socialMediaService;
    private final SocialMediaMapper socialMediaMapper;

    @GetMapping("/my-social-media")
    @Operation(summary = "Get all social media links for authenticated user")
    public ResponseEntity<List<SocialMediaDto>> getAllSocialsForUser(
            @AuthenticationPrincipal CustomPrincipal principal) {
        var userid = principal.getId();
        var socialMedas = socialMediaService.getAllSocialMediaLinksForUserId(userid);
        var socialMediaDtos = socialMedas.stream().map(socialMediaMapper::toSocialMediaDto).toList();
        return ResponseEntity.ok(socialMediaDtos);
    }

    @PostMapping()
    public ResponseEntity<SocialMediaDto> createSocialMediaForUser(
            @AuthenticationPrincipal CustomPrincipal principal,
            @RequestBody CreateSocialMediaDto request,
            UriComponentsBuilder uriComponentsBuilder) {
        var userId = principal.getId();
        var newSocial = socialMediaService.createSocialMediaForUser(userId, request);
        var socialMediaDto = socialMediaMapper.toSocialMediaDto(newSocial);
        var locationUri = uriComponentsBuilder
                .path("/profiles/socialMedia/{id}")
                .buildAndExpand(socialMediaDto.getId())
                .toUri();
        return ResponseEntity.created(locationUri).body(socialMediaDto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<SocialMediaDto> updateSocialMediaForUser(
            @RequestBody CreateSocialMediaDto request,
            @AuthenticationPrincipal CustomPrincipal principal,
            @PathVariable Long id) {
        var userId = principal.getId();
        var updatedSocial = socialMediaService.updateSocialMediaForUser(userId, id, request);
        var socialMediaDto = socialMediaMapper.toSocialMediaDto(updatedSocial);
        return ResponseEntity.ok(socialMediaDto);
    }
}
