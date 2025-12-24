package com.rizvi.jobee.services;

import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rizvi.jobee.dtos.interview.ConductorDto;
import com.rizvi.jobee.entities.BusinessAccount;
import com.rizvi.jobee.entities.Interview;
import com.rizvi.jobee.entities.InterviewPreparationResource;
import com.rizvi.jobee.entities.Job;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailSender {
  private final ResendService resendService;

  @Value("${email.from}")
  private String senderEmail;

  public void sendBusinessAccountVerificationEmail(String email, String verificationCode, String fullName) {
    try {
      String subject = "Verify Your Business Account on Jobee";
      String htmlString = generateBusinessAccountVerificationEmailHtml(fullName, verificationCode);
      resendService.sendEmail(email, subject, htmlString);
    } catch (Exception e) {
      System.out.println("Failed to send business account verification email: " + e.getMessage());
    }
  }

  public void sendInterviewPrepResourcesEmail(Set<InterviewPreparationResource> resources, String companyName,
      String jobTitle, String candidateName,
      String candidateEmail) {
    try {
      String subject = "Your Interview Preparation Resources for " + jobTitle + " at " + companyName;
      String htmlString = generateInterviewPrepResourcesEmailHtml(candidateName, jobTitle, companyName, resources);
      resendService.sendEmail(candidateEmail, subject, htmlString);
    } catch (Exception e) {
      System.out.println("Failed to send rejection email: " + e.getMessage());
    }
  }

  public void sendRejectionEmail(Interview interview) {
    String to = interview.getCandidateEmail();
    String fullName = interview.getCandidate().getFullName();
    String jobTitle = interview.getJob().getTitle();
    String companyName = interview.getCreatedBy().getCompany().getName();
    String reasonForRejection = interview.getRejection().getReason();
    String feedbackForRejection = interview.getRejection().getFeedback();
    try {
      String subject = "Update on your application for " + jobTitle + " at " + companyName;
      String htmlString = generateRejectionEmailHtml(fullName, jobTitle, companyName, reasonForRejection,
          feedbackForRejection);
      resendService.sendEmail(to, subject, htmlString);
    } catch (Exception e) {
      System.out.println("Failed to send rejection email: " + e.getMessage());
    }
  }

  public void sendDocumentViaEmail(String fullName, String email, String fileUrl, String otherPartyName,
      boolean isDocument) {
    String to = email;
    try {
      String emailSubject = otherPartyName + ": Requested Document from Jobee";
      String emailHtml = generateMessageAttachmentEmailHtml(fullName, otherPartyName);
      String attachmentFileName = isDocument ? "attachment.pdf" : "attachment.jpg";
      resendService.sendEmailWithAttachment(to, emailSubject, emailHtml, fileUrl, attachmentFileName);
    } catch (Exception e) {
      System.out.println("Failed to send message attachment email: " + e.getMessage());
    }
  }

  public void sendUpdatedInterviewEmail(
      Interview interview, Set<BusinessAccount> newInterviewers, Set<ConductorDto> newOtherInterviewers,
      Set<BusinessAccount> removedInterviewers, Set<ConductorDto> removedOtherInterviewers,
      Set<BusinessAccount> unchangedInterviewers, Set<ConductorDto> unchangedOtherInterviewers) {
    String to = interview.getCandidateEmail();
    String fullName = interview.getCandidate().getFullName();
    String jobTitle = interview.getJob().getTitle();
    String interviewDate = interview.getInterviewDate().toString();
    String companyName = interview.getCreatedBy().getCompany().getName();

    try {
      String candidateSubject = "IMPORTANT: Your interview for " + jobTitle + " at " + companyName
          + " has been updated";
      String candidateHtml = generateUpdatedInterviewEmailForCandidate(fullName, jobTitle, interviewDate,
          companyName);
      resendService.sendEmail(to, candidateSubject, candidateHtml);
    } catch (Exception e) {
      System.out.println("Failed to send scheduled interview email: " + e.getMessage());
    }

    // Send email to conductors who are on Jobee + creator
    String conductorSubject = "Updated regarding the interview for " + jobTitle;
    for (BusinessAccount conductor : newInterviewers) {
      String conductorHtml = generateScheduledInterviewEmailForConductorWithAccount(
          interview.getCreatedBy(), jobTitle, interviewDate, companyName, conductor);
      try {
        resendService.sendEmail(conductor.getEmail(), conductorSubject, conductorHtml);
      } catch (Exception e) {
        System.out.println("Failed to send scheduled interview email to conductor: " + e.getMessage());
      }
    }
    for (BusinessAccount conductor : removedInterviewers) {
      try {
        String conductorHtml = generateConductorWithAccountRemovedFromInterviewEmail(
            interview.getCreatedBy(), jobTitle, companyName, conductor);
        resendService.sendEmail(conductor.getEmail(), conductorSubject, conductorHtml);
      } catch (Exception e) {
        System.out.println("Failed to send scheduled interview email to conductor: " + e.getMessage());
      }

    }
    for (BusinessAccount conductor : unchangedInterviewers) {
      try {
        String conductorHtml = generateUpdatedInterviewEmailForConductorWithAccount(
            interview.getCreatedBy(), jobTitle, companyName, conductor);
        resendService.sendEmail(conductor.getEmail(), conductorSubject, conductorHtml);
      } catch (Exception e) {
        System.out.println("Failed to send scheduled interview email to conductor: " + e.getMessage());
      }
    }
  }

  public void sendInterviewCancellationEmail(Interview interview) {
    String to = interview.getCandidateEmail();
    String fullName = interview.getCandidate().getFullName();
    String jobTitle = interview.getJob().getTitle();
    String companyName = interview.getJob().getCompany().getName();
    String cancellationReason = interview.getCancellationReason();

    try {
      String candidateSubject = "Your interview for " + jobTitle + " at " + companyName + " has been cancelled";
      String candidateHtml = generateInterviewCancellationEmailHtml(fullName, jobTitle, companyName,
          cancellationReason);
      resendService.sendEmail(to, candidateSubject, candidateHtml);
    } catch (Exception e) {
      System.out.println("Failed to send interview cancellation email: " + e.getMessage());
    }

    // Send email to conductors who are on Jobee + creator
    String conductorSubject = "Interview Cancelled for " + jobTitle;
    Set<BusinessAccount> validConductors = interview.getInterviewers();
    validConductors.add(interview.getCreatedBy());
    validConductors.forEach(conductor -> {
      try {
        String conductorHtml = generateInterviewCancellationEmailForConductorWithAccount(
            interview.getCreatedBy(), jobTitle, companyName, cancellationReason, conductor);
        resendService.sendEmail(conductor.getEmail(), conductorSubject, conductorHtml);
      } catch (Exception e) {
        System.out.println("Failed to send interview cancellation email to conductor: " + e.getMessage());
      }
    });

    // TODO: Send email to conductors who are not on Jobee
  }

  public void sendScheduledInterviewEmail(Interview interview) {
    String to = interview.getCandidateEmail();
    String fullName = interview.getCandidate().getFullName();
    String jobTitle = interview.getJob().getTitle();
    String interviewDate = interview.getInterviewDate().toString();
    String companyName = interview.getCreatedBy().getCompany().getName();

    try {
      String candidateSubject = "You have a scheduled interview for " + jobTitle + " at " + companyName;
      String candidateHtml = generateScheduledInterviewEmailForCandidate(fullName, jobTitle, interviewDate,
          companyName);
      resendService.sendEmail(to, candidateSubject, candidateHtml);
    } catch (Exception e) {
      System.out.println("Failed to send scheduled interview email: " + e.getMessage());
    }

    // Send email to conductors who are on Jobee + creator
    String conductorSubject = "Interview Scheduled to Conduct for " + jobTitle;
    Set<BusinessAccount> validConductors = interview.getInterviewers();
    validConductors.add(interview.getCreatedBy());
    validConductors.forEach(conductor -> {
      try {
        String conductorHtml = generateScheduledInterviewEmailForConductorWithAccount(
            interview.getCreatedBy(), jobTitle, interviewDate, companyName, conductor);
        resendService.sendEmail(conductor.getEmail(), conductorSubject, conductorHtml);
      } catch (Exception e) {
        System.out.println("Failed to send scheduled interview email to conductor: " + e.getMessage());
      }
    });

    // TODO: Send email to conductors who are not on Jobee

  }

  public void sendInterviewPrepEmail(Interview interview) {
    String to = interview.getCandidateEmail();
    String fullName = interview.getCandidate().getFullName();
    String jobTitle = interview.getJob().getTitle();
    String companyName = interview.getJob().getCompany().getName();
    String interviewDate = interview.getInterviewDate().toString();

    try {
      String subject = "Your Interview Preparation Materials are Ready for " + jobTitle;
      String htmlString = generateInterviewPrepHtml(fullName, jobTitle, interviewDate, companyName);
      resendService.sendEmail(to, subject, htmlString);
    } catch (Exception e) {
      System.out.println("Failed to send interview preparation email: " + e.getMessage());
    }
  }

  public void sendHiringTeamInvitationEmail(BusinessAccount to, BusinessAccount from, Job job) {
    try {
      String toEmail = to.getEmail();
      String inviterFullName = from.getFullName();
      String jobTitle = job.getTitle();
      String companyName = job.getCompany().getName();

      String subject = "Invitation to join the hiring team for job: " + jobTitle;
      String jobeeUrl = generateJobeeUrl();
      String htmlString = generateHiringTeamInvitationHtml(companyName, inviterFullName, jobTitle, jobeeUrl);
      resendService.sendEmail(toEmail, subject, htmlString);
    } catch (Exception e) {
      System.out.println("Failed to send hiring team invitation email: " + e.getMessage());
    }
  }

  public void sendHiringTeamInvitationAndJoinJobeeEmail(
      String to, String companyName, BusinessAccount sender, String companyCode, String jobTitle,
      String inviteUrl, String qrCode) {

    try {
      String inviteeFullName = sender.getFullName();
      String subject = "You're invited to join the hiring team for " + jobTitle + " at " + companyName
          + " on Jobee!";
      String htmlString = generateHiringTeamInvitationAndJoinJobeeHtml(
          companyName, inviteeFullName, jobTitle, inviteUrl, companyCode, qrCode);
      resendService.sendEmail(to, subject, htmlString);
    } catch (Exception e) {
      // TODO: handle exception
    }
  }

  public void sendInvitationEmail(
      String to, String companyName, String inviteeFullName, String invitationType,
      String companyCode, String inviteUrl, String qrCode) {
    try {
      String subject = "You're invited to join " + companyName + " on Jobee!";
      String htmlString = generateInvitationHtml(companyName, inviteeFullName, inviteUrl, qrCode, companyCode);
      resendService.sendEmail(to, subject, htmlString);
    } catch (Exception e) {
      System.out.println("Failed to send email: " + e.getMessage());
    }
  }

  private String generateJobeeUrl() {
    return "https://jobee.com";
  }

  private String generateBusinessAccountVerificationEmailHtml(String fullName, String verificationCode) {
    String htmlString = """
        <html>
          <body style="font-family: Arial, sans-serif; background-color: #f6f9fc; padding: 40px; text-align: center;">
            <div style="max-width: 600px; margin: auto; background: white; border-radius: 10px; padding: 40px;">
              <h2 style="color: #2d3748;">Verify Your Business Account on Jobee</h2>
              <p style="color: #4a5568;">Hello %s,</p>
              <p style="color: #4a5568;">
                Thank you for creating a business account on <strong>Jobee</strong>. Please use the verification code below to verify your account.
              </p>
              <p style="margin-top: 20px; color: #4a5568; font-size: 24px">
                Verification Code: <strong>%s</strong>
              </p>
              <p style="margin-top: 40px; font-size: 12px; color: #a0aec0;">
                If you didn't expect this email, you can safely ignore it.
              </p>
            </div>
          </body>
        </html>
        """
        .formatted(fullName, verificationCode);

    return htmlString;
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
              <p style="margin-top: 20px; color: #4a5568; font-size: 24px">
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

  private String generateHiringTeamInvitationHtml(String companyName, String inviterFullName, String jobTitle,
      String jobeeUrl) {
    String htmlString = """
        <html>
          <body style="font-family: Arial, sans-serif; background-color: #f6f9fc; padding: 40px; text-align: center;">
            <div style="max-width: 600px; margin: auto; background: white; border-radius: 10px; padding: 40px;">
              <h2 style="color: #2d3748;">You're invited to join the hiring team for %s at %s on Jobee!</h2>
              <p style="color: #4a5568;">Hello,</p>
              <p style="color: #4a5568;">
                <strong>%s</strong> has invited you to join %s's hiring team on <strong>Jobee</strong>.
              </p>
              <p style="margin-top: 40px; font-size: 12px; color: #a0aec0;">
                If you did not expect this invitation, you can safely ignore this email.
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
                View on Jobee
              </a>
            </div>
            </div>
          </body>
        </html>
        """
        .formatted(
            jobTitle,
            companyName,
            inviterFullName,
            companyName,
            jobeeUrl);

    return htmlString;
  }

  private String generateHiringTeamInvitationAndJoinJobeeHtml(
      String companyName, String inviteeFullName, String jobTitle, String inviteUrl, String companyCode,
      String qrCodeUrl) {
    String htmlString = """
        <html>
          <body style="font-family: Arial, sans-serif; background-color: #f6f9fc; padding: 40px; text-align: center;">
            <div style="max-width: 600px; margin: auto; background: white; border-radius: 10px; padding: 40px;">
              <h2 style="color: #2d3748;">You're invited to join the hiring team for %s at %s on Jobee!</h2>
              <p style="color: #4a5568;">Hello,</p>
              <p style="color: #4a5568;">
                <strong>%s</strong> has invited you to join %s's hiring team on <strong>Jobee</strong>. You need to make an account first.
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
            jobTitle,
            companyName,
            inviteeFullName,
            companyName,
            companyCode,
            inviteUrl,
            qrCodeUrl != null ? "<img src=\"" + qrCodeUrl
                + "\" alt=\"QR Code\" style=\"margin-top: 20px; width: 150px; height: 150px;\"/>" : "");

    return htmlString;
  }

  private String generateScheduledInterviewEmailForCandidate(String fullName, String jobTitle, String interviewDate,
      String companyName) {
    String htmlString = """
                        <html>
                <body style="font-family: Arial, sans-serif; background-color: #f6f9fc; padding: 40px; text-align: center;">
                  <div style="max-width: 600px; margin: auto; background: white; border-radius: 10px; padding: 40px;">
                    <h2 style="color: #2d3748;">You have been scheduled for an Interview at %s!</h2>
                    <p style="color: #4a5568;">Hello %s</p>
                    <p style="color: #4a5568;">
                      <strong>%s</strong> has invited you for an interview for <strong>%s</strong>. Please review details on Jobee.
                    </p>
                    <p style="color: #4a5568;">
                      Your interview is scheduled for <strong>%s</strong>.
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
                      View on Jobee
                    </a>
                    </div>
                    <p style="margin-top: 40px; font-size: 12px; color: #a0aec0;">
                      If you didn't expect this invitation, you can safely ignore this email.
                    </p>
                  </div>
                </body>
              </html>
        """
        .formatted(companyName, fullName, companyName, jobTitle, interviewDate, "https://google.com");
    return htmlString;
  }

  private String generateInterviewCancellationEmailForConductorWithAccount(
      BusinessAccount interviewCreator, String jobTitle, String companyName, String cancellationReason,
      BusinessAccount conductorAccount) {
    String createdBy = interviewCreator.getFullName();
    String conductor = conductorAccount.getFullName();
    String htmlString = """
                        <html>
                <body style="font-family: Arial, sans-serif; background-color: #f6f9fc; padding: 40px; text-align: center;">
                  <div style="max-width: 600px; margin: auto; background: white; border-radius: 10px; padding: 40px;">
                    <h2 style="color: #2d3748;">Interview for %s has been cancelled</h2>
                    <p style="color: #4a5568;">Hello %s,</p>
                    <p style="color: #4a5568;">
                      You were supposed to conduct theinterview for the position of <strong>%s</strong> at <strong>%s</strong>.
                      However, the interview has been cancelled by <strong>%s</strong>.
                    </p>
                    <p style="color: #4a5568;">
                      Reason for cancellation: <strong>%s</strong>
                    </p>
                    <p style="margin-top: 40px; font-size: 12px; color: #a0aec0;">
                      If you have any questions, feel free to reach out to our support team.
                    </p>
                      <div style="
                        display: inline-block;
                        margin: 20px 0;
                        padding: 14px 28px;
                        background-color: #21c55e;
                        color: white;
                        font-weight: bold;
                        border-radius: 6px;
                        text-decoration: none;">
                        View on Jobee
                      </div>
                  </div>
                </body>
              </html>
        """
        .formatted(jobTitle, conductor, jobTitle, companyName, createdBy, cancellationReason);
    return htmlString;
  }

  private String generateUpdatedInterviewEmailForCandidate(String fullName, String jobTitle, String interviewDate,
      String companyName) {
    String htmlString = """
              <html>
                <body style="font-family: Arial, sans-serif; background-color: #f6f9fc; padding: 40px; text-align: center;">
                  <div style="max-width: 600px; margin: auto; background: white; border-radius: 10px; padding: 40px;">
                    <h2 style="color: #2d3748;">Your Interview for %s has been Updated!</h2>
                    <p style="color: #4a5568;">Hello %s,</p>
                    <p style="color: #4a5568;">
                      Your interview for the position of <strong>%s</strong> at <strong>%s</strong> has been updated. Please review the new details on Jobee.
                    </p>
                    <div>
                    <a href="%s" style="
                        display: inline-block;
                        margin: 20px 0;
                        padding: 14px 28px;
                        background-color: #2563eb;
                        color: white;
                        font-weight: bold;
                        border-radius: 6px;
                        text-decoration: none;">
                      View on Jobee
                    </a>
                    </div>
                    <p style="margin-top: 40px; font-size: 12px; color: #a0aec0;">
                      If you didn't expect this update, you can safely ignore this email.
                    </p>
                  </div>
                </body>
              </html>
        """
        .formatted(jobTitle, fullName, jobTitle, companyName, "https://google.com");
    return htmlString;
  }

  private String generateUpdatedInterviewEmailForConductorWithAccount(
      BusinessAccount createdBy, String jobTitle, String companyName, BusinessAccount conductor) {
    String creatorName = createdBy.getFullName();
    String conductorName = conductor.getFullName();
    Long creatorId = createdBy.getId();
    Long conductorId = conductor.getId();
    boolean isSelfCreated = creatorId.equals(conductorId);
    if (isSelfCreated) {
      return """
          <html>
            <body style="font-family: Arial, sans-serif; background-color: #f6f9fc; padding: 40px; text-align: center;">
              <div style="max-width: 600px; margin: auto; background: white; border-radius: 10px; padding: 40px;">
                <h2 style="color: #2d3748;">Interview Updated for %s</h2>
                <p style="color: #4a5568;">Hello %s,</p>
                <p style="color: #4a5568;">
                  Your interview for <strong>%s</strong> has been successfully updated.
                </p>
                <p style="color: #4a5568;">
                  Please review the updated details on Jobee.
                </p>
                <div>
                  <a href="%s" style="
                      display: inline-block;
                      margin: 20px 0;
                      padding: 14px 28px;
                      background-color: #2563eb;
                      color: white;
                      font-weight: bold;
                      border-radius: 6px;
                      text-decoration: none;">
                    View on Jobee
                  </a>
                </div>
                <p style="margin-top: 40px; font-size: 12px; color: #a0aec0;">
                  If you did not expect this email, you can safely ignore it.
                </p>
              </div>
            </body>
          </html>
          """
          .formatted(
              jobTitle,
              creatorName,
              jobTitle,
              "https://google.com");
    } else {
      return """
          <html>
            <body style="font-family: Arial, sans-serif; background-color: #f6f9fc; padding: 40px; text-align: center;">
              <div style="max-width: 600px; margin: auto; background: white; border-radius: 10px; padding: 40px;">
                <h2 style="color: #2d37448;">Interview Update for %s</h2>
                <p style="color: #4a5568;">Hello %s,</p>
                <p style="color: #4a5568;">
                  <strong>%s</strong> has updated the interview details for the <strong>%s</strong> role.
                </p>
                <p style="color: #4a5568;">
                 Please review details on Jobee.
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
                    View on Jobee
                  </a>
                </div>
                <p style="margin-top: 40px; font-size: 12px; color: #a0aec0;">
                  If you didn't expect this update, you can safely ignore this email.
                </p>
              </div>
            </body>
          </html>
          """
          .formatted(
              jobTitle,
              conductorName,
              creatorName,
              jobTitle,
              "https://google.com");
    }
  }

  private String generateConductorWithAccountRemovedFromInterviewEmail(
      BusinessAccount createdBy, String jobTitle, String companyName, BusinessAccount conductor) {
    String creatorName = createdBy.getFullName();
    String conductorName = conductor.getFullName();
    return """
        <html>
          <body style="font-family: Arial, sans-serif; background-color: #f6f9fc; padding: 40px; text-align: center;">
            <div style="max-width: 600px; margin: auto; background: white; border-radius: 10px; padding: 40px;">
              <h2 style="color: #2d3748;">You have been removed from the Interview for %s</h2>
              <p style="color: #4a5568;">Hello %s,</p>
              <p style="color: #4a5568;">
                <strong>%s</strong> has removed you from conducting the interview for the <strong>%s</strong> role.
              </p>
              <p style="margin-top: 40px; font-size: 12px; color: #a0aec0;">
                If you have any questions, feel free to reach out to our support team.
              </p>
                <div>
                  <a href="%s" style="
                      display: inline-block;
                      margin: 20px 0;
                      padding: 14px 28px;
                      background-color: #2563eb;
                      color: white;
                      font-weight: bold;
                      border-radius: 6px;
                      text-decoration: none;">
                    View on Jobee
                  </a>
                </div>
            </div>
          </body>
        </html>
        """
        .formatted(
            jobTitle,
            conductorName,
            creatorName,
            jobTitle,
            "https://google.com");
  }

  private String generateScheduledInterviewEmailForConductorWithAccount(
      BusinessAccount interviewCreator,
      String jobTitle,
      String interviewDate,
      String companyName,
      BusinessAccount conductor) {

    String creatorName = interviewCreator.getFullName();
    String conductorName = conductor.getFullName();
    Long creatorId = interviewCreator.getId();
    Long conductorId = conductor.getId();

    boolean isSelfCreated = creatorId.equals(conductorId);

    if (isSelfCreated) {
      return """
          <html>
            <body style="font-family: Arial, sans-serif; background-color: #f6f9fc; padding: 40px; text-align: center;">
              <div style="max-width: 600px; margin: auto; background: white; border-radius: 10px; padding: 40px;">
                <h2 style="color: #2d3748;">Interview Created for %s</h2>
                <p style="color: #4a5568;">Hello %s,</p>
                <p style="color: #4a5568;">
                  Your interview for <strong>%s</strong> has been successfully scheduled.
                </p>
                <p style="color: #4a5568;">
                  The interview is set for <strong>%s</strong>.
                </p>
                <div>
                  <a href="%s" style="
                      display: inline-block;
                      margin: 20px 0;
                      padding: 14px 28px;
                      background-color: #2563eb;
                      color: white;
                      font-weight: bold;
                      border-radius: 6px;
                      text-decoration: none;">
                    View on Jobee
                  </a>
                </div>
                <p style="margin-top: 40px; font-size: 12px; color: #a0aec0;">
                  If you did not expect this email, you can safely ignore it.
                </p>
              </div>
            </body>
          </html>
          """
          .formatted(
              jobTitle,
              creatorName,
              jobTitle,
              interviewDate,
              "https://google.com");
    }
    return """
        <html>
          <body style="font-family: Arial, sans-serif; background-color: #f6f9fc; padding: 40px; text-align: center;">
            <div style="max-width: 600px; margin: auto; background: white; border-radius: 10px; padding: 40px;">
              <h2 style="color: #2d3748;">You Are Invited to Conduct an Interview for %s!</h2>
              <p style="color: #4a5568;">Hello %s,</p>
              <p style="color: #4a5568;">
                <strong>%s</strong> has scheduled an interview and invited you to conduct it for the <strong>%s</strong> role.
              </p>
              <p style="color: #4a5568;">
                The interview is scheduled for <strong>%s</strong>.
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
                  View on Jobee
                </a>
              </div>
              <p style="margin-top: 40px; font-size: 12px; color: #a0aec0;">
                If you didn't expect this invitation, you can safely ignore this email.
              </p>
            </div>
          </body>
        </html>
        """
        .formatted(
            jobTitle,
            conductorName,
            creatorName,
            jobTitle,
            interviewDate,
            "https://google.com");
  }

  private String generateInterviewCancellationEmailHtml(String fullName, String jobTitle, String companyName,
      String cancellationReason) {
    String htmlString = """
                        <html>
                <body style="font-family: Arial, sans-serif; background-color: #f6f9fc; padding: 40px; text-align: center;">
                  <div style="max-width: 600px; margin: auto; background: white; border-radius: 10px; padding: 40px;">
                    <p style="color: #4a5568;">Hello %s,</p>
                    <p style="color: #4a5568;">
                      We regret to inform you that your interview for the position of <strong>%s</strong> at <strong>%s</strong> has been cancelled.
                    </p>
                    <p style="color: #4a5568;">
                      Reason for cancellation: <strong>%s</strong>
                    </p>
                    <p style="margin-top: 40px; font-size: 12px; color: #a0aec0;">
                      If you have any questions, feel free to reach out to our support team.
                    </p>
                      <div style="
                        display: inline-block;
                        margin: 20px 0;
                        padding: 14px 28px;
                        background-color: #21c55e;
                        color: white;
                        font-weight: bold;
                        border-radius: 6px;
                        text-decoration: none;">
                      View on Jobee
                    </div>
                  </div>
                </body>
              </html>
        """
        .formatted(fullName, jobTitle, companyName, cancellationReason);
    return htmlString;
  }

  private String generateRejectionEmailHtml(String fullName, String jobTitle, String companyName,
      String reasonForRejection, String feedbackForRejection) {
    String htmlString = """
                        <html>
                <body style="font-family: Arial, sans-serif; background-color: #f6f9fc; padding: 40px; text-align: center;">
                  <div style="max-width: 700px; margin: auto; background: white; border-radius: 10px; padding: 40px;">
                    <p style="color: #4a5568;">Hello %s,</p>
                    <p style="color: #4a5568;">
                      We appreciate your interest in the %s position at %s. After careful consideration, we regret to inform you that we will not be moving forward with your application.
                    </p>
                    <p style="color: #4a5568;">
                      Reason for rejection: <strong>%s</strong>
                    </p>
                    <p style="color: #4a5568;">
                      Feedback: <strong>%s</strong>
                    <p style="color: #4a5568;">
                      We encourage you to apply for future openings that match your skills and experience. Thank you again for considering a career with us.
                    </p>
                    <p style="margin-top: 40px; font-size: 12px; color: #a0aec0;">
                      If you have any questions, feel free to reach out to our support team.
                    </p>
                  </div>
                    <div style="
                        display: inline-block;
                        margin: 20px 0;
                        padding: 14px 28px;
                        background-color: #21c55e;
                        color: white;
                        font-weight: bold;
                        border-radius: 6px;
                        text-decoration: none;">
                      View on Jobee
                    </div>
                  </div>
                </body>
              </html>
        """
        .formatted(fullName, jobTitle, companyName, reasonForRejection,
            feedbackForRejection.isEmpty() ? "No Additional Feedback" : feedbackForRejection);
    return htmlString;
  }

  private String generateInterviewPrepHtml(String fullName, String jobTitle, String interviewDate,
      String companyName) {
    String htmlString = """
                        <html>
                <body style="font-family: Arial, sans-serif; background-color: #f6f9fc; padding: 40px; text-align: center;">
                  <div style="max-width: 600px; margin: auto; background: white; border-radius: 10px; padding: 40px;">
                    <h2 style="color: #2d3748;">Your interview preparation materials are ready for an Interview at %s!</h2>
                    <p style="color: #4a5568;">Hello %s</p>
                    <p style="color: #4a5568;">
                      <strong>Jobee</strong> has prepared your materials for your interview at <strong>%s</strong> for %s. Please review details on Jobee.
                    </p>
                  <div style="
                        display: inline-block;
                        margin: 20px 0;
                        padding: 14px 28px;
                        background-color: #21c55e;
                        color: white;
                        font-weight: bold;
                        border-radius: 6px;
                        text-decoration: none;">
                      View on Jobee
                    </div>
                    <p style="margin-top: 40px; font-size: 12px; color: #a0aec0;">
                      If you didn't expect this invitation, you can safely ignore this email.
                    </p>
                  </div>
                </body>
              </html>
        """
        .formatted(companyName, fullName, companyName, jobTitle, interviewDate);
    return htmlString;
  }

  private String generateMessageAttachmentEmailHtml(String fullName, String otherPartyName) {
    String htmlString = """
                        <html>
                <body style="font-family: Arial, sans-serif; background-color: #f6f9fc; padding: 40px; text-align: center;">
                  <div style="max-width: 600px; margin: auto; background: white; border-radius: 10px; padding: 40px;">
                  <h2 style="color: #4a5568;">Hello %s,</h2>
                    <p style="color: #2d3748;">You requested an email copy of one of the documents regarding %s.</p>
                    <p style="color: #4a5568;">
                      Please check the attached file..
                    </p>
                    <p style="margin-top: 40px; font-size: 12px; color: #a0aec0;">
                      If you didn't expect this attachment, you can safely ignore this email.
                    </p>
                  </div>
                </body>
              </html>
        """
        .formatted(fullName, otherPartyName);
    return htmlString;
  }

  private String generateInterviewPrepResourcesEmailHtml(
      String candidateName,
      String jobTitle,
      String companyName,
      Set<InterviewPreparationResource> resources) {

    StringBuilder resourcesListBuilder = new StringBuilder();

    for (InterviewPreparationResource resource : resources) {
      resourcesListBuilder.append("""
            <tr>
              <td style="padding: 16px 0;">
                <table width="100%%" cellpadding="0" cellspacing="0"
                       style="border: 1px solid #e5e7eb; border-radius: 8px; background-color: #f9fafb;">
                  <tr>
                    <td style="padding: 16px;">
                      <a href="%s"
                         style="color: #2563eb; text-decoration: none; font-size: 16px; font-weight: 600;">
                        %s
                      </a>
                      <p style="margin: 8px 0 0 0; color: #4b5563; font-size: 14px; line-height: 1.5;">
                        %s
                      </p>
                    </td>
                  </tr>
                </table>
              </td>
            </tr>
          """.formatted(
          resource.getLink(),
          resource.getTitle(),
          resource.getDescription()));
    }

    return """
          <html>
            <body style="margin: 0; padding: 0; background-color: #f3f4f6; font-family: Arial, sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0">
                <tr>
                  <td align="center" style="padding: 40px 16px;">
                    <table width="600" cellpadding="0" cellspacing="0"
                           style="background-color: #ffffff; border-radius: 12px; overflow: hidden;">

                      <!-- Header -->
                      <tr>
                        <td style="background-color: #2563eb; padding: 24px;">
                          <h1 style="margin: 0; color: #ffffff; font-size: 22px;">
                            Interview Prep Resources
                          </h1>
                        </td>
                      </tr>

                      <!-- Content -->
                      <tr>
                        <td style="padding: 32px;">
                          <p style="margin: 0 0 12px 0; color: #374151; font-size: 16px;">
                            Hi %s,
                          </p>

                          <p style="margin: 0 0 16px 0; color: #4b5563; font-size: 15px; line-height: 1.6;">
                            <strong>Jobee</strong> has prepared interview preparation materials
                            for your upcoming interview at <strong>%s</strong>
                            for the <strong>%s</strong> role.
                          </p>

                          <p style="margin: 24px 0 12px 0; color: #374151; font-size: 16px; font-weight: 600;">
                            Recommended Resources
                          </p>

                          <table width="100%%" cellpadding="0" cellspacing="0">
                            %s
                          </table>

                          <p style="margin: 32px 0 0 0; color: #6b7280; font-size: 13px; line-height: 1.5;">
                            Best of luck — we’re rooting for you! 🚀
                          </p>

                          <p style="margin: 8px 0 0 0; color: #6b7280; font-size: 13px;">
                            — The Jobee Team
                          </p>
                        </td>
                      </tr>

                    </table>
                  </td>
                </tr>
              </table>
            </body>
          </html>
        """.formatted(
        candidateName,
        companyName,
        jobTitle,
        resourcesListBuilder.toString());
  }

}
