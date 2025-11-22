package com.rizvi.jobee.dtos.experience;

import lombok.Data;

@Data
public class ExperienceDto {
    private Long id;
    private String title;
    private String description;
    private String company;
    private String city;
    private String state;
    private String country;
    private String from;
    private String to;
    private Boolean currentlyWorking;
}
