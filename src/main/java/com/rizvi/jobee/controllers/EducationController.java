package com.rizvi.jobee.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rizvi.jobee.dtos.EducationDto;

import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;

// TODO: Implement the methods
@RestController
@AllArgsConstructor
@RequestMapping("/profiles/education")
public class EducationController {
    @PostMapping
    @Operation(summary = "Add education information")
    public ResponseEntity<EducationDto> addEducation() {
        return ResponseEntity.ok(new EducationDto());
    }
}
