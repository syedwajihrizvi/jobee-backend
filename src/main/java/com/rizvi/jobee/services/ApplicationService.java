package com.rizvi.jobee.services;

import org.springframework.stereotype.Service;

import com.rizvi.jobee.entities.Application;
import com.rizvi.jobee.exceptions.ApplicationNotFoundException;
import com.rizvi.jobee.repositories.ApplicationRepository;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class ApplicationService {
    private final ApplicationRepository applicationRepository;

    public Application findById(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new ApplicationNotFoundException("Application not found with id: " + id));
    }
}