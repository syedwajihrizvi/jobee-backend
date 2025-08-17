package com.rizvi.jobee.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@Data
@ConfigurationProperties(prefix = "cloud.aws")
public class AWSProperties {
    private String region;
    private String bucket;
    private String accessKey;
    private String secretKey;
}
