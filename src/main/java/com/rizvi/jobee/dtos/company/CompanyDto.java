package com.rizvi.jobee.dtos.company;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class CompanyDto {
    private Long id;
    private String name;
    private String website;
    private Integer foundedYear;
    private BigDecimal rating;
    private Integer numEmployees;
    private String industry;
    private String description;
    private String hqCity;
    private String hqState;
    private String hqCountry;
    private String location;
}
