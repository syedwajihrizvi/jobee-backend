package com.rizvi.jobee.services;

import org.springframework.stereotype.Service;

import com.rizvi.jobee.interfaces.NotificationService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class FCMService implements NotificationService {

    @Override
    public void sendNotification(String to, String title, String body) {
        System.out.println("Sending notification to: " + to);
    }

}
