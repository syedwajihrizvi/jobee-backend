package com.rizvi.jobee.dtos.user;

import lombok.Data;

@Data
public class CreateDocViaLinkDto {
    private String documentLink;
    private String documentType;
    private String documentTitle;
    private String documentUrlType;
}
