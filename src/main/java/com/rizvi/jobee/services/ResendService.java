package com.rizvi.jobee.services;

import org.springframework.stereotype.Service;

import com.resend.*;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;

@Service
public class ResendService {
    private final Resend resend;

    public ResendService(String apiKey) {
        this.resend = new Resend(apiKey);
    }

    public void sendEmail(String to, String subject, String htmlConent) throws ResendException {
        System.out.println("SYED-DEBUG: Sending Resend Email to " + to);
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
}
