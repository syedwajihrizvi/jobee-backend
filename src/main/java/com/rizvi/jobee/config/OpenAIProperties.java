package com.rizvi.jobee.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@Data
@ConfigurationProperties(prefix = "openai")
public class OpenAIProperties {
    private String apiKey;
}
