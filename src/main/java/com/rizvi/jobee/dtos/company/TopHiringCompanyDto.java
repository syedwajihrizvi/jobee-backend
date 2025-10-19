package com.rizvi.jobee.dtos.company;

import lombok.Data;

@Data
public class TopHiringCompanyDto {
    private Long id;
    private String name;
    private Long jobCount;
    private String logoUrl;
}