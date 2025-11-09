package com.rizvi.jobee.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rizvi.jobee.entities.BusinessAccount;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;

@Service
@RequiredArgsConstructor
public class EmailSender {
    private final SesClient sesClient;

    @Value("${email.from}")
    private String senderEmail;

    public void sendHiringTeamInvitationEmail() {
        System.out.println("Sending hiring team invitation email...");
    }

    public void sendHiringTeamInvitationAndJoinJobbeeEmail() {
        System.out.println("Sending hiring team invitation and join Jobee email...");
    }

    public void sendInvitationEmail(
            String to, String companyName, BusinessAccount sender, String invitationType,
            String companyCode, String inviteUrl, String qrCode) {
        try {
            Destination destination = createEmailDestination(to);
            String inviteeFullName = sender.getFullName();
            System.out.println("Destination created for: " + to);
            String subject = "You're invited to join " + companyName + " on Jobee!";
            String htmlString = generateInvitationHtml(companyName, inviteeFullName, inviteUrl, qrCode, companyCode);
            String textString = inviteeFullName + "has invited you to join " + companyName + " on Jobee as an "
                    + invitationType + "!";
            Message message = createEmail(subject, htmlString, textString);
            SendEmailRequest emailRequest = SendEmailRequest.builder()
                    .source("Jobee <" + senderEmail + ">")
                    .destination(destination)
                    .message(message)
                    .replyToAddresses("support@jobee.solutions")
                    .build();
            sesClient.sendEmail(emailRequest);
        } catch (Exception e) {
            // TODO: handle exception
            System.out.println("Failed to send email: " + e.getMessage());
        }
    }

    private Destination createEmailDestination(String to) {
        // TODO: use temp email until verified
        return Destination.builder().toAddresses("wajih@jobee.solutions").build();
    }

    private Message createEmail(String subject, String htmlString, String textString) {
        Content subjectContent = createContent(subject);
        Content htmlContent = createContent(htmlString);
        Content textContent = createContent(textString);
        Body bodyContent = createEmailBody(htmlContent, textContent);
        return Message.builder().subject(subjectContent).body(bodyContent).build();
    }

    private Content createContent(String data) {
        return Content.builder().charset("UTF-8").data(data).build();
    }

    private Body createEmailBody(Content htmlContent, Content textContent) {
        return Body.builder().html(htmlContent).text(textContent).build();
    }

    private String generateInvitationHtml(
            String companyName, String senderName, String inviteUrl, String qrCodeUrl, String companyCode) {
        String htmlString = """
                <html>
                  <body style="font-family: Arial, sans-serif; background-color: #f6f9fc; padding: 40px; text-align: center;">
                    <div style="max-width: 600px; margin: auto; background: white; border-radius: 10px; padding: 40px;">
                      <h2 style="color: #2d3748;">You're invited to join %s on Jobee!</h2>
                      <p style="color: #4a5568;">Hello,</p>
                      <p style="color: #4a5568;">
                        <strong>%s</strong> has invited you to join their company workspace on <strong>Jobee</strong>.
                      </p>
                      <p style="color: #4a5568;">
                        Use the button below to join and enter the company code or scan the QR code.
                      </p>
                      <p style="margin-top: 20px; color: #4a5568;">
                        Company Code: <strong>%s</strong>
                      </p>
                    <div>
                    <a href="%s" style="
                          display: inline-block;
                          margin: 20px 0;
                          padding: 14px 28px;
                          background-color: #21c55e;
                          color: white;
                          font-weight: bold;
                          border-radius: 6px;
                          text-decoration: none;">
                        Join on Jobee
                      </a>
                    </div>

                      %s

                      <p style="margin-top: 40px; font-size: 12px; color: #a0aec0;">
                        If you didn't expect this invitation, you can safely ignore this email.
                      </p>
                    </div>
                  </body>
                </html>
                """
                .formatted(
                        companyName,
                        senderName,
                        companyCode,
                        inviteUrl,
                        qrCodeUrl != null ? "<img src=\"" + qrCodeUrl
                                + "\" alt=\"QR Code\" style=\"margin-top: 20px; width: 150px; height: 150px;\"/>" : "");

        return htmlString;

    }

    private String generateCompanyCode(String companyName) {
        return "COMPANY-CODE-123";
    }

    private String generateInviteUrl(String companyCode) {
        return "http://example.com/invite/";
    }
}
