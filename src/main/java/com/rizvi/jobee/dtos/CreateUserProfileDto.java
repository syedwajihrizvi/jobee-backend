package com.rizvi.jobee.dtos;

import lombok.Data;

@Data
public class CreateUserProfileDto {
    private String firstName;
    private String lastName;
    private Integer age;
    private Long accountId;
}
