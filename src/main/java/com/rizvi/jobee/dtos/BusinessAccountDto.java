package com.rizvi.jobee.dtos;

import lombok.Data;

@Data
public class BusinessAccountDto {
    private Long id;
    private String email;
    private String companyName;
    private Long companyId;
    private String firstName;
    private String lastName;
}
