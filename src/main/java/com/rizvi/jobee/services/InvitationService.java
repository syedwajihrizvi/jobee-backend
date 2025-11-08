package com.rizvi.jobee.services;

import java.security.SecureRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rizvi.jobee.entities.BusinessAccount;
import com.rizvi.jobee.entities.Invitation;
import com.rizvi.jobee.enums.BusinessType;
import com.rizvi.jobee.enums.InvitationStatus;
import com.rizvi.jobee.helpers.QRUtils;
import com.rizvi.jobee.repositories.InvitationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InvitationService {
    private final InvitationRepository invitationRepository;
    @Autowired
    private EmailSender emailSender;
    @Autowired
    private SMSService smsService;

    private String generateCompanyCode(String companyName) {
        // Implement your company code generation logic here
        String charPool = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(charPool.charAt(random.nextInt(charPool.length())));
        }
        return companyName.substring(0, 0) + "-" + code.toString().toLowerCase();
    }

    private String generateInviteLink(String companyCode) {
        // Implement your invite link generation logic here
        return "https://jobee.com/invite?code=" + companyCode.replace(" ", "-").toLowerCase();
    }

    private String generateQRCodeUrl(String inviteLink) throws Exception {
        // Implement your QR code URL generation logic here
        return QRUtils.generateQRCodeBase64(inviteLink, 150, 150);
    }

    public Invitation createInvitation(String email, String phoneNumber, String selectedUserType,
            BusinessAccount invitedBy) {
        var companyName = invitedBy.getCompany().getName();
        // Create the invitation and the fields
        Invitation invitation = new Invitation();
        invitation.setEmail(email);
        invitation.setPhoneNumber(phoneNumber);
        invitation.setInvitationType(BusinessType.valueOf(selectedUserType));
        invitation.setInvitedBy(invitedBy);
        invitation.setCompany(invitedBy.getCompany());

        // Generate a company code
        String companyCode = generateCompanyCode(companyName);
        invitation.setCompanyCode(companyCode);
        // Generate a invite link
        String inviteLink = generateInviteLink(companyCode);
        // Generate QR code URL
        String qrCodeUrl = null;
        try {
            qrCodeUrl = generateQRCodeUrl(inviteLink);
        } catch (Exception e) {
            // Handle exception
            System.out.println("Failed to generate QR code: " + e.getMessage());
        }
        if (email != null && !email.isEmpty()) {
            emailSender.sendInvitationEmail(email, companyName, invitedBy,
                    selectedUserType, companyCode, inviteLink, qrCodeUrl);
        }
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            smsService.sendInviteSMS(phoneNumber, companyName, invitedBy, selectedUserType);
        }
        var savedInvitation = invitationRepository.save(invitation);
        return savedInvitation;
    }

    public Invitation getInvitationByCompanyCode(String companyCode) {
        System.out.println("Fetching invitation for company code: " + companyCode);
        return invitationRepository.findByCompanyCode(companyCode);
    }

    public void updateInvitationStatus(Invitation invitation, InvitationStatus status) {
        invitation.setStatus(status);
        invitationRepository.save(invitation);
    }
}
