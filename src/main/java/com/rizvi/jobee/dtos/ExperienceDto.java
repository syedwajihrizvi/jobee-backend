package com.rizvi.jobee.dtos;

import lombok.Data;

@Data
public class ExperienceDto {
    private Long id;
    private String title;
    private String description;
    private String company;
    private String location;
    private Integer from;
    private Integer to;
    private Boolean currentlyWorking;
}
