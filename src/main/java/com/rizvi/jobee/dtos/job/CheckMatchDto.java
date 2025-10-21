package com.rizvi.jobee.dtos.job;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CheckMatchDto {
    private Long match;
    private List<String> reasons;

    public CheckMatchDto(Long match, List<String> reasons) {
        this.match = match;
        this.reasons = reasons;
    }
}
