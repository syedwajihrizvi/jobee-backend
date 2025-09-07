package com.rizvi.jobee.dtos.user;

import lombok.Data;

@Data
public class CreateBusinessAccountDto {
    private String email;
    private String password;
    private String companyName;
}
