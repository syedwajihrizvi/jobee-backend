package com.rizvi.jobee.dtos.invitations;

import lombok.Data;

@Data
public class CreateInvitationDto {
    private String email;
    private String phoneNumber;
    private String selectedUserType;
}
