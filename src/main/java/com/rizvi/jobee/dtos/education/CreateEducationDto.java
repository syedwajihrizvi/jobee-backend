package com.rizvi.jobee.dtos.education;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateEducationDto {
    private String degree;
    private String institution;
    private String fromYear;
    private String toYear;
}
