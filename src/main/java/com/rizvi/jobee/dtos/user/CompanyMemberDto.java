package com.rizvi.jobee.dtos.user;

import lombok.Data;

@Data
public class CompanyMemberDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String userType;
    private String joinedDate;
    private String profileImageUrl;
    private Boolean isMe;
}
