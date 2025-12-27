package com.rizvi.jobee.dtos.application;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class JobOfferDto {
    private String offerDetails;
    private LocalDateTime offerMade;
    private boolean accepted;
    private boolean userAction;
}
