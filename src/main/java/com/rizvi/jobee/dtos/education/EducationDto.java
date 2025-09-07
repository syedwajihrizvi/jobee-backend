package com.rizvi.jobee.dtos.education;

import lombok.Data;

@Data
public class EducationDto {
    private Long id;
    private String degree;
    private String institution;
    private Integer fromYear;
    private Integer toYear;
}
