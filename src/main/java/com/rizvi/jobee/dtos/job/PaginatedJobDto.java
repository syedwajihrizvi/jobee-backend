package com.rizvi.jobee.dtos.job;

import java.util.List;

import com.rizvi.jobee.entities.Job;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaginatedJobDto {
    private boolean hasMore;
    private List<Job> jobs;
    private Long totalElements;

    public PaginatedJobDto(boolean hasMore, List<Job> jobs) {
        this.hasMore = hasMore;
        this.jobs = jobs;
    }
}
