package com.rizvi.jobee.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.rizvi.jobee.services.ResendService;

@Configuration
public class ResendConfig {

    @Value("${resend.apiKey}")
    private String apiKey;

    @Bean
    public ResendService resendService() {
        return new ResendService(apiKey);
    }
}
