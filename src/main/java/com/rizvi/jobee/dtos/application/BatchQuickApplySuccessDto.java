package com.rizvi.jobee.dtos.application;

import java.net.URI;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BatchQuickApplySuccessDto {
    private List<ApplicationDto> applications;
    private List<URI> uris;

}