package com.rizvi.jobee.dtos.application;

import java.util.List;

import lombok.Data;

@Data
public class BatchQuickApplyDto {
    private List<Long> jobIds;
}
