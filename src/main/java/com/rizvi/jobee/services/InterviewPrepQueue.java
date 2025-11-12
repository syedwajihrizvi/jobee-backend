package com.rizvi.jobee.services;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.rizvi.jobee.entities.InterviewPreparation;
import com.rizvi.jobee.entities.Notification;
import com.rizvi.jobee.helpers.AISchemas.PrepareForInterviewRequest;
import com.rizvi.jobee.helpers.AISchemas.PrepareForInterviewResponse;
import com.rizvi.jobee.interfaces.NotificationService;
import com.rizvi.jobee.mappers.NotificationMapper;
import com.rizvi.jobee.repositories.InterviewPreparationRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class InterviewPrepQueue {
    private final NotificationService notificationService;
    private final AIService aiService;
    private final InterviewPreparationRepository interviewPreparationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserNotificationService userNotificationService;
    private final NotificationMapper notificationMapper;
    private final EmailSender emailSender;

    @Async
    public void processInterviewPrep(PrepareForInterviewRequest prepareForInterviewRequest,
            InterviewPreparation interviewPrep) {
        try {
            PrepareForInterviewResponse response = aiService.generateInterviewPrep(prepareForInterviewRequest);
            // Update the interview prep for the intreview in DB with the response
            interviewPrep.updateViaAIResponse(response);
            var savedInterview = interviewPreparationRepository.save(interviewPrep);
            notificationService.sendNotification("user-device-token", "Interview Prep Ready",
                    "Your interview preparation materials are ready for interview");
            // Notify using WebSocket
            var interviewerId = savedInterview.getInterview().getCandidate().getId();
            String recepientDest = "/topic/notifications/user/" + interviewerId;
            Notification savedNotification = userNotificationService
                    .createInterviewPrepNotificationAndSend(interviewPrep);
            var notificationDto = notificationMapper.toNotificationDto(savedNotification);
            messagingTemplate.convertAndSend(recepientDest, notificationDto);
            // Send Email Notification
            emailSender.sendInterviewPrepEmail(interviewPrep);
        } catch (Exception e) {
            // TODO: Handle the exception properly
            System.out.println("Interview prep processing was interrupted");
            System.out.println(e.getMessage());
        }
    }
}
