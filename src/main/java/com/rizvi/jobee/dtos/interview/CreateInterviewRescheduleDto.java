package com.rizvi.jobee.dtos.interview;

import java.time.LocalDate;
import java.time.LocalTime;

import com.rizvi.jobee.enums.Timezone;

import lombok.Data;

@Data
public class CreateInterviewRescheduleDto {
    private LocalDate interviewDate;
    private LocalTime startTime;
    private Timezone timezone;
    private String reason;
}
