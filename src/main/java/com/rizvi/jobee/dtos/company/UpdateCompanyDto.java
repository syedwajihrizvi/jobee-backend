package com.rizvi.jobee.dtos.company;

import lombok.Data;

@Data
public class UpdateCompanyDto {
    private String name;
    private Integer foundedYear;
    private String industry;
    private String hqCity;
    private String hqState;
    private String hqCountry;
    private Integer numEmployees;
    private String description;
    private String website;
}
