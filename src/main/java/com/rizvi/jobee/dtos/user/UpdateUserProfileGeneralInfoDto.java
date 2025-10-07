package com.rizvi.jobee.dtos.user;

import lombok.Data;

@Data
public class UpdateUserProfileGeneralInfoDto {
    private String firstName;
    private String lastName;
    private String title;
    private String company;
    private String email;
    private String phoneNumber;
    private String city;
    private String country;
    private String state;
    private String province;
}
