package com.rizvi.jobee.dtos.experience;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ExperienceDto {
    private Long id;
    private String title;
    private String description;
    private String company;
    private String city;
    private String state;
    private String country;
    private String location;
    private String from;
    private String to;
    private Boolean currentlyWorking;
}
