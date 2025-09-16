package com.rizvi.jobee.services;

import org.springframework.stereotype.Service;

import com.openai.client.OpenAIClient;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class AIService {
    private final OpenAIClient openAIClient;
}