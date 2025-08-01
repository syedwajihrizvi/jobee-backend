package com.rizvi.Autohub.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Component
@Data
@ConfigurationProperties(prefix = "spring.jwt")
public class JwtConfig {
    @NotBlank(message = "JWT secret must not be blank")
    private String secret;

    @Value("${spring.jwt.accessTokenExpiration:86400000}") // Default to 1 day in milliseconds
    private long accessTokenExpiration;

    @Value("${spring.jwt.refreshTokenExpiration:604800000}") // Default to 7 days in milliseconds
    private long refreshTokenExpiration;
}
