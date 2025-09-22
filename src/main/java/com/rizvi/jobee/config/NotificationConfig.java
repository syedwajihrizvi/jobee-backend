package com.rizvi.jobee.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.rizvi.jobee.intefaces.NotificationService;
import com.rizvi.jobee.services.FCMService;

@Configuration
public class NotificationConfig {

    @Bean
    public NotificationService notificationService() {
        return new FCMService();
    }
}
