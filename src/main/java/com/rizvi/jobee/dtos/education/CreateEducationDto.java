package com.rizvi.jobee.dtos.education;

import lombok.Data;

@Data
public class CreateEducationDto {
    private String degree;
    private String institution;
    private String fromYear;
    private String toYear;
}
