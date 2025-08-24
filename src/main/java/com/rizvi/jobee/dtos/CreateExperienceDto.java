package com.rizvi.jobee.dtos;

import lombok.Data;

@Data
public class CreateExperienceDto {
    private String title;
    private String description;
    private String company;
    private String city;
    private String country;
    private Integer from;
    private Integer to;
}
