package com.rizvi.jobee.dtos.user;

import lombok.Data;

@Data
public class UpdateBusinessProfileGeneralInfoDto {
    private String firstName;
    private String lastName;
    private String title;
    private String email;
    private String city;
    private String country;
    private String state;
}
