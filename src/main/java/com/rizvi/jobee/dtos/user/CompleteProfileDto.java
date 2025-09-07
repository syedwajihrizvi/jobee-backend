package com.rizvi.jobee.dtos.user;

import lombok.Data;

@Data
public class CompleteProfileDto {
    private String title;
    private String summary;
    private String city;
    private String country;
    private String phoneNumber;
    private String company;
    private String position;

}
