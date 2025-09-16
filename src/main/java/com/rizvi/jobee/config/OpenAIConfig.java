package com.rizvi.jobee.config;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
public class OpenAIConfig {

    private final OpenAIProperties openAIProperties;

    @Bean
    public OpenAIClient openAIClient() {
        return OpenAIOkHttpClient.builder().apiKey(openAIProperties.getApiKey()).build();
    }
}
