package com.rizvi.jobee.dtos.application;

import lombok.Data;

@Data
public class CreateApplicationDto {
    private Long jobId;
    private Long resumeDocumentId;
    private Long coverLetterDocumentId;
}
