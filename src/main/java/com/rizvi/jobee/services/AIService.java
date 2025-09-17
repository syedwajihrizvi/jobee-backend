package com.rizvi.jobee.services;

import org.springframework.stereotype.Service;

import com.openai.client.OpenAIClient;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class AIService {
    private final OpenAIClient openAIClient;

    public void extractDetailsFromResume(String resumeText) {
        // Use openAIClient to process the resume text and extract details
        // This is a placeholder for actual implementation
        // Exract skills, experience, education, etc.
    }
}