package com.rizvi.jobee.dtos.job;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaginatedResponse<T> {
    private boolean hasMore;
    private List<T> content;
    private Long totalElements;

    public PaginatedResponse(boolean hasMore, List<T> content) {
        this.hasMore = hasMore;
        this.content = content;
    }
}
