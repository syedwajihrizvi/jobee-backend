package com.rizvi.jobee.dtos.user;

import java.util.ArrayList;
import java.util.List;

import com.rizvi.jobee.dtos.socialMedia.SocialMediaDto;
import com.rizvi.jobee.enums.CompanyVerificationStatus;

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
    private CompanyVerificationStatus companyVerified;
    private Boolean verified;
    private String profileImageUrl;
    private String role;
    private List<SocialMediaDto> socialMedias = new ArrayList<>();
}
