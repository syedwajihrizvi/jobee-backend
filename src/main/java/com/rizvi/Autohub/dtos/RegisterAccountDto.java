package com.rizvi.Autohub.dtos;

import lombok.Data;

@Data
public class RegisterAccountDto {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
}
