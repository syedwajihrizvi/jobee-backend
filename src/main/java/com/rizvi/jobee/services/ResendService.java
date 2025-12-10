package com.rizvi.jobee.services;

import org.springframework.stereotype.Service;

import com.resend.*;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.Attachment;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;

@Service
public class ResendService {
    private final Resend resend;

    public ResendService(String apiKey) {
        this.resend = new Resend(apiKey);
    }

    public void sendEmail(String to, String subject, String htmlConent) throws ResendException {
        CreateEmailOptions request = CreateEmailOptions.builder().from("Jobee <no-reply@jobee.solutions>").to(to)
                .subject(subject).html(htmlConent).html(htmlConent).build();
        try {
            CreateEmailResponse response = resend.emails().send(request);
            System.out.println("Email sent with ID: " + response.getId());
        } catch (Exception e) {
            System.out.println("Failed to send email: " + e.getMessage());
            throw e;
        }
    }

    public void sendEmailWithAttachment(String to, String subject, String htmlContent, String fileUrl,
            String fileName) {
        Attachment att = Attachment.builder()
                .path(fileUrl)
                .fileName(fileName)
                .build();
        System.out.println("Preparing to send email with attachment to: " + to + " | File URL: " + fileUrl);
        CreateEmailOptions params = CreateEmailOptions.builder().from("Jobee <no-reply@jobee.solutions>").to(to)
                .subject("Your Message Attachment from Jobee").html(htmlContent)
                .addAttachment(att)
                .build();

        try {
            CreateEmailResponse response = resend.emails().send(params);
            System.out.println("Email with attachment sent with ID: " +
                    response.getId());
        } catch (Exception e) {
            System.out.println("Failed to send email with attachment: " + e.getMessage());
        }
    }
}
