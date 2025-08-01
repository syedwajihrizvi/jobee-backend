package com.rizvi.Autohub.dtos;

import lombok.Data;

@Data
public class AccountSummaryDto {
    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private String createdAt;
}
