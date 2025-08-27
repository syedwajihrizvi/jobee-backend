package com.rizvi.jobee.dtos;

import java.util.List;

import lombok.Data;

@Data
public class JobDetailedSummaryForBusinessDto {
    private Long id;
    private String title;
    private String description;
    private List<ApplicationDto> applications;
}
