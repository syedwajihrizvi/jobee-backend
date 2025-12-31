package com.rizvi.jobee.services;

import java.security.SecureRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rizvi.jobee.entities.BusinessAccount;
import com.rizvi.jobee.entities.HiringTeam;
import com.rizvi.jobee.entities.Invitation;
import com.rizvi.jobee.entities.Job;
import com.rizvi.jobee.enums.BusinessType;
import com.rizvi.jobee.enums.InvitationStatus;
import com.rizvi.jobee.helpers.QRUtils;
import com.rizvi.jobee.repositories.InvitationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InvitationService {
    private final InvitationRepository invitationRepository;
    private final S3Service s3Service;
    @Autowired
    private EmailSender emailSender;

    public Invitation createIntitationForHiringMember(HiringTeam teamMember, BusinessAccount invitedBy) {
        Invitation invitation = new Invitation();
        invitation.setEmail(teamMember.getEmail());
        invitation.setInvitationType(BusinessType.EMPLOYEE);
        invitation.setInvitedBy(invitedBy);
        invitation.setCompany(invitedBy.getCompany());

        String companyName = invitedBy.getCompany().getName();
        String companyCode = generateCompanyCode(companyName);
        invitation.setCompanyCode(companyCode);
        var savedInvitation = invitationRepository.save(invitation);
        return savedInvitation;
    }

    private String generateCompanyCode(String companyName) {
        // Implement your company code generation logic here
        String charPool = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            code.append(charPool.charAt(random.nextInt(charPool.length())));
        }
        return companyName.substring(0, 1) + "-" + code.toString().toUpperCase();
    }

    private String generateInviteLink(String companyCode) {
        // Implement your invite link generation logic here
        return "https://jobee.com/invite?code=" + companyCode.replace(" ", "-").toUpperCase();
    }

    private String generateQRCodeUrl(String inviteLink) throws Exception {
        // Implement your QR code URL generation logic here
        return QRUtils.generateQRCodeBase64(inviteLink, 150, 150);
    }

    public Invitation createInvitation(String email, String phoneNumber, String selectedUserType,
            BusinessAccount invitedBy) {
        var companyName = invitedBy.getCompany().getName();
        Invitation invitation = new Invitation();
        invitation.setEmail(email);
        invitation.setPhoneNumber(phoneNumber);
        invitation.setInvitationType(BusinessType.valueOf(selectedUserType));
        invitation.setInvitedBy(invitedBy);
        invitation.setCompany(invitedBy.getCompany());

        String companyCode = generateCompanyCode(companyName);
        invitation.setCompanyCode(companyCode);
        var savedInvitation = invitationRepository.save(invitation);
        String inviteLink = generateInviteLink(companyCode);
        String qrCodeUrl = null;
        String s3Url = null;
        try {
            qrCodeUrl = generateQRCodeUrl(inviteLink);
            s3Url = s3Service.uploadQRCodeViaURL(savedInvitation.getId(), qrCodeUrl);
        } catch (Exception e) {
            // Handle exception
            System.out.println("Failed to generate QR code: " + e.getMessage());
        }
        if (email != null && !email.isEmpty()) {
            emailSender.sendInvitationEmail(email, companyName, invitedBy.getFullName(),
                    selectedUserType, companyCode, inviteLink, s3Url);
        }
        return savedInvitation;
    }

    public Invitation getInvitationByCompanyCode(String companyCode) {
        return invitationRepository.findByCompanyCode(companyCode);
    }

    public void updateInvitationStatus(Invitation invitation, InvitationStatus status) {
        invitation.setStatus(status);
        invitationRepository.save(invitation);
    }

    public void sendHiringTeamInvitationEmail(BusinessAccount to, BusinessAccount from, Job job) {
        emailSender.sendHiringTeamInvitationEmail(to, from, job);
    }

    public void sendHiringTeamInvitationAndJoinJobeeEmail(String senderName,
            HiringTeam hiringTeamMember, Invitation invitation, String jobTitle, String companyName) {
        var companyCode = invitation.getCompanyCode();
        var inviteLink = generateInviteLink(companyCode);
        String qrCode = null;
        String s3Url = null;
        try {
            qrCode = generateQRCodeUrl(inviteLink);
            s3Url = s3Service.uploadQRCodeViaURL(invitation.getId(), qrCode);
        } catch (Exception e) {
            System.out.println("SYED-ERROR: Error generating QR code: " + e.getMessage());
        }
        String to = hiringTeamMember.getEmail();
        String recipientName = hiringTeamMember.getFullName();
        emailSender.sendHiringTeamInvitationAndJoinJobeeEmail(
                to, companyName, senderName, companyCode, jobTitle, s3Url, recipientName);
    }

}
