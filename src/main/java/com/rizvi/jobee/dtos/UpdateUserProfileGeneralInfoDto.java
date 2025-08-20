package com.rizvi.jobee.dtos;

import lombok.Data;

@Data
public class UpdateUserProfileGeneralInfoDto {
    private String firstName;
    private String lastName;
    private String title;
    private String company;
    private String email;
    private String phone;
    private String city;
    private String country;
}
