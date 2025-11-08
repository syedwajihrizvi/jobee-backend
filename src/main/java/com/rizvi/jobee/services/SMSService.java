package com.rizvi.jobee.services;

import org.springframework.stereotype.Service;

import com.rizvi.jobee.config.TwilioProperties;
import com.rizvi.jobee.entities.BusinessAccount;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SMSService {

    private final TwilioProperties twilioProperties;

    public void sendInviteSMS(String phoneNumber, String companyName, BusinessAccount businessAccount,
            String invitationType) {
        String inviteUrl = "https://jobee.com/invite";
        String companyCode = "ABC123";
        String fullName = businessAccount.getFullName();
        String body = fullName + " has invited you to join " + companyName + " on Jobee as a " + invitationType +
                ".\nUse this link to sign up: " + inviteUrl + "\n" + "Company Code to enter: " + companyCode;
        System.out.println("Sending SMS to: " + phoneNumber);
        Message sms = Message.creator(
                new PhoneNumber(phoneNumber),
                new PhoneNumber(twilioProperties.getPhoneNumber()),
                body).create();
        System.out.println("SMS sent with SID: " + sms.getSid());
    }
}
