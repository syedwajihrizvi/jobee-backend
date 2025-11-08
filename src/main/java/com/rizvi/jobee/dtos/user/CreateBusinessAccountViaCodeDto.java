package com.rizvi.jobee.dtos.user;

import lombok.Data;

@Data
public class CreateBusinessAccountViaCodeDto {
    private String companyCode;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String password;
}
