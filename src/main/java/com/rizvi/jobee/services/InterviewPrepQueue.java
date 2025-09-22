package com.rizvi.jobee.services;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.rizvi.jobee.intefaces.NotificationService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class InterviewPrepQueue {
    private final NotificationService notificationService;

    @Async
    public void processInterviewPrep(Long interviewId) {
        // Temporarily make this method run for 10 seconds to simulate processing
        try {
            Thread.sleep(10000);
            notificationService.sendNotification("user-device-token", "Interview Prep Ready",
                    "Your interview preparation materials are ready for interview ID: " + interviewId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
