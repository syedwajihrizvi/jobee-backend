package com.rizvi.jobee.dtos.interview;

import lombok.Data;

@Data
public class CreateInterviewRejectionDto {
    private String reason;
    private String feedback;
}
