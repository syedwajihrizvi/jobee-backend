package com.rizvi.jobee.dtos.interview;

import lombok.Data;

@Data
public class RequestRescheduleDto {
    private String reason;
    private String interviewDate;
    private String startTime;
    private Boolean viewed;
    private String timezone;
}
