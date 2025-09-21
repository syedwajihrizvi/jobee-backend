package com.rizvi.jobee.dtos.job;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaginatedJobResponseDto<T> {
    private boolean hasMore;
    private List<T> content;
}
