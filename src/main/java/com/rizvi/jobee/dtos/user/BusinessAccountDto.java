package com.rizvi.jobee.dtos.user;

import lombok.Data;

@Data
public class BusinessAccountDto {
    private Long id;
    private String email;
    private String companyName;
    private Long companyId;
    private String companyLogo;
    private String firstName;
    private String lastName;
    private String title;
    private String location;
    private String city;
    private String state;
    private String country;
    private Boolean verified;
    private String profileImageUrl;
    private String role;
}
