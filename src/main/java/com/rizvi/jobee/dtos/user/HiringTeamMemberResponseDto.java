package com.rizvi.jobee.dtos.user;

import lombok.Data;

@Data
public class HiringTeamMemberResponseDto {
    private String email;
    private String firstName;
    private String lastName;
    private String profileImageUrl;
    private Boolean verified;
}
