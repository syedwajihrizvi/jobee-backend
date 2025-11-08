package com.rizvi.jobee.config;

import org.springframework.context.annotation.Configuration;

import com.twilio.Twilio;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class TwilioConfig {

    private final TwilioProperties twilioProperties;

    @PostConstruct
    public void initTwilio() {
        Twilio.init(twilioProperties.getAccountSid(), twilioProperties.getAuthToken());

    }
}
