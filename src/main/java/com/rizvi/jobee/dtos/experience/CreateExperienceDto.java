package com.rizvi.jobee.dtos.experience;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateExperienceDto {
    private String title;
    private String description;
    private String company;
    private String city;
    private String state;
    private String country;
    private String from;
    private String to;
}
