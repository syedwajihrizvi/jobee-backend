package com.rizvi.jobee.dtos.company;

import lombok.Data;

@Data
public class CompanyDto {
    private Long id;
    private String name;
    private String website;
    private Integer foundedYear;
    private Integer numEmployees;
    private String industry;
    private String description;
    private String location;
}
