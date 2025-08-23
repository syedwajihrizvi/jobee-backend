package com.rizvi.jobee.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rizvi.jobee.repositories.ExperienceRepository;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/profiles/experience")
public class ExperienceController {
    private final ExperienceRepository experienceRepository;

    // TODO: Implement the methods
    // GET, POST, PUT, DELTETE
}
