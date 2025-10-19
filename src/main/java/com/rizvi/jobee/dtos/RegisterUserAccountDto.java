package com.rizvi.jobee.dtos;

import lombok.Data;

@Data
public class RegisterUserAccountDto {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private Integer age;

}
