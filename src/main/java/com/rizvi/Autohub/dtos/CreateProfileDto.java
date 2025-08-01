package com.rizvi.Autohub.dtos;

import lombok.Data;

@Data
public class CreateProfileDto {
    private String bio;
    private Integer accountId;
    private String profilePictureUrl;
}
