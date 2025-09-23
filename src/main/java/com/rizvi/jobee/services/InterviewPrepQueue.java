package com.rizvi.jobee.services;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.rizvi.jobee.entities.InterviewPreparation;
import com.rizvi.jobee.helpers.AISchemas.PrepareForInterviewRequest;
import com.rizvi.jobee.helpers.AISchemas.PrepareForInterviewResponse;
import com.rizvi.jobee.intefaces.NotificationService;
import com.rizvi.jobee.repositories.InterviewPreparationRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class InterviewPrepQueue {
    private final NotificationService notificationService;
    private final AIService aiService;
    private final InterviewPreparationRepository interviewPreparationRepository;

    @Async
    public void processInterviewPrep(PrepareForInterviewRequest prepareForInterviewRequest,
            InterviewPreparation interviewPrep) {
        try {
            PrepareForInterviewResponse response = aiService.generateInterviewPrep(prepareForInterviewRequest);
            // Update the interview prep for the intreview in DB with the response
            interviewPrep.updateViaAIResponse(response);
            interviewPreparationRepository.save(interviewPrep);
            notificationService.sendNotification("user-device-token", "Interview Prep Ready",
                    "Your interview preparation materials are ready for interview");
        } catch (Exception e) {
            // TODO: Handle the exception properly
            System.out.println("Interview prep processing was interrupted");
            System.out.println(e.getMessage());
        }
    }
}
