package com.rizvi.jobee.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateExperienceDto {
    private String title;
    private String description;
    private String company;
    private String city;
    private String country;
    private String from;
    private String to;
}
