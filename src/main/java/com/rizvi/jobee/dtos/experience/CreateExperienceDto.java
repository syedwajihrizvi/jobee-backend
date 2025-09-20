package com.rizvi.jobee.dtos.experience;

import lombok.Data;

@Data
public class CreateExperienceDto {
    private String title;
    private String description;
    private String company;
    private String city;
    private String country;
    private String from;
    private String to;
}
